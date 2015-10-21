/**
 *  Copyright 2012 Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * File: org.bgp4j.netty.fsm.InternalFSM.java
 */
package com.jive.oss.bgp.netty.fsm;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.jive.oss.bgp.config.nodes.PeerConfiguration;
import com.jive.oss.bgp.netty.FSMState;

import lombok.extern.slf4j.Slf4j;

/**
 * Internal FSM to seperate FSM logic from the connection management and message handling code.
 *
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@Slf4j
public class InternalFSM
{

  private FSMState state = FSMState.Idle;
  private PeerConfiguration peerConfiguration;
  private InternalFSMCallbacks callbacks;

  private int connectRetryCounter = 0;
  private boolean canAcceptConnection = false;

  private final FireEventTimeManager<FireConnectRetryTimerExpired> fireConnectRetryTimeExpired;
  private FireEventTimeManager<FireIdleHoldTimerExpired> fireIdleHoldTimerExpired;
  private FireEventTimeManager<FireDelayOpenTimerExpired> fireDelayOpenTimerExpired;
  private FireEventTimeManager<FireHoldTimerExpired> fireHoldTimerExpired;
  private FireRepeatedEventTimeManager<FireAutomaticStart> fireRepeatedAutomaticStart;
  private FireEventTimeManager<FireSendKeepalive> fireKeepaliveTimerExpired;

  private int peerProposedHoldTime = 0;
  private boolean haveFSMError = false;
  private long lastConnectStamp = 0;

  private InternalFSMChannelManager connectedChannelManager;
  private InternalFSMChannelManager activeChannelManager;

  InternalFSM(Scheduler scheduler)
  {
    this.fireConnectRetryTimeExpired = new FireEventTimeManager<>(scheduler);
    fireIdleHoldTimerExpired = new FireEventTimeManager<>(scheduler);
    fireDelayOpenTimerExpired = new FireEventTimeManager<>(scheduler);
    fireHoldTimerExpired = new FireEventTimeManager<>(scheduler);
    fireRepeatedAutomaticStart = new FireRepeatedEventTimeManager<>(scheduler);
    fireKeepaliveTimerExpired = new FireEventTimeManager<>(scheduler);

  }

  void setup(final PeerConfiguration peerConfiguration, final InternalFSMCallbacks callbacks) throws SchedulerException
  {

    this.peerConfiguration = peerConfiguration;
    this.callbacks = callbacks;

    this.fireConnectRetryTimeExpired.createJobDetail(FireConnectRetryTimerExpired.class, this);
    this.fireIdleHoldTimerExpired.createJobDetail(FireIdleHoldTimerExpired.class, this);
    this.fireDelayOpenTimerExpired.createJobDetail(FireDelayOpenTimerExpired.class, this);
    this.fireHoldTimerExpired.createJobDetail(FireHoldTimerExpired.class, this);
    this.fireKeepaliveTimerExpired.createJobDetail(FireSendKeepalive.class, this);
    this.fireRepeatedAutomaticStart.createJobDetail(FireAutomaticStart.class, this);

    this.connectedChannelManager = new InternalFSMChannelManager(callbacks);
    this.activeChannelManager = new InternalFSMChannelManager(callbacks);
  }

  void destroyFSM()
  {
    try
    {
      this.fireConnectRetryTimeExpired.shutdown();
      this.fireIdleHoldTimerExpired.shutdown();
      this.fireDelayOpenTimerExpired.shutdown();
      this.fireHoldTimerExpired.shutdown();
      this.fireRepeatedAutomaticStart.shutdown();
      this.fireKeepaliveTimerExpired.shutdown();
    }
    catch (final SchedulerException e)
    {
      this.log.error("Internal error: failed to shutdown internal FSM for peer " + this.peerConfiguration.getPeerName(), e);
    }
  }

  void handleEvent(final FSMEvent event)
  {
    FSMChannel channel = null;
    InternalFSMChannelManager channelManager = null;

    if (event instanceof FSMEvent.ChannelFSMEvent)
    {
      channel = ((FSMEvent.ChannelFSMEvent) event).getChannel();

      if (this.connectedChannelManager.isManagedChannel(channel))
      {
        channelManager = this.connectedChannelManager;
      }
      else if (this.activeChannelManager.isManagedChannel(channel))
      {
        channelManager = this.activeChannelManager;
      }
    }

    switch (event.getType())
    {
      case AutomaticStart:
      case ManualStart:
        this.handleStartEvent(event.getType());
        break;
      case AutomaticStop:
      case ManualStop:
        this.handleStopEvent(event.getType());
        break;
      case ConnectRetryTimer_Expires:
        this.handleConnectRetryTimerExpiredEvent();
        break;
      case IdleHoldTimer_Expires:
        this.handleIdleHoldTimerExpiredEvent();
        break;
      case TcpConnectionConfirmed:
        if (channel != null)
        {
          this.handleTcpConnectionConfirmed(channel);
        }
        else
        {
          this.haveFSMError = true;
        }
        break;
      case Tcp_CR_Acked:
        if (channel != null)
        {
          this.handleTcpConnectionAcked(channel);
        }
        else
        {
          this.haveFSMError = true;
        }
        break;
      case TcpConnectionFails:
        if (channel != null)
        {
          this.handleTcpConnectionFails(channel);
        }
        else
        {
          this.haveFSMError = true;
        }
        break;
      case DelayOpenTimer_Expires:
        this.handleDelayOpenTimerExpiredEvent();
        break;
      case HoldTimer_Expires:
        this.handleHoldTimerExpiredEvent();
        break;
      case BGPOpen:
        if ((channel != null) && (channelManager != null))
        {
          this.handleBgpOpenEvent(channel, channelManager);
        }
        else
        {
          this.haveFSMError = true;
        }
        break;
      case KeepAliveMsg:
        this.handleKeepaliveMessageEvent();
        break;
      case KeepaliveTimer_Expires:
        this.handleKeepaliveTimerExpiresEvent();
        break;
      case NotifyMsg:
        this.handleNotifyMessageEvent();
        break;
      case NotifyMsgVerErr:
        this.handleNotifyMessageVersionErrorEvent();
        break;
      case BGPOpenMsgErr:
        this.handleBGPOpenMessageErrorEvent();
        break;
      case BGPHeaderErr:
        this.handleBGPHeaderErrorEvent();
        break;
      case UpdateMsg:
        this.handleUpdateMessageEvent();
        break;
      case UpdateMsgErr:
        this.handleUpdateMessageErrorEvent();
        break;
    }

    if (channelManager != null)
    {
      channelManager.pushInboundFSMEvent(event.getType());
    }

    if (this.haveFSMError)
    {
      // sent internal error notification only when in state established or open confirm or open sent
      switch (this.state)
      {
        case Established:
        case OpenConfirm:
        case OpenSent:
          this.connectedChannelManager.fireSendInternalErrorNotification();
          this.activeChannelManager.fireSendInternalErrorNotification();
          break;
      }

      this.connectRetryCounter++;

      this.moveStateToIdle();

      this.haveFSMError = false;
    }
  }

  /**
   * handle any kind of start event. Unless the FSM is in <code>Idle</code> state the event is ignored
   * <ul>
   * <li>If passive TCP estalishment is disabled then fire the connect remote peer callback and move to <code>Connect</code> state</li>
   * <li>If passive TCP estalishment is ensabled then move to <code>Connect</code> state</li>
   * </ul>
   *
   * @param fsmEventType
   * @throws SchedulerException
   */
  private void handleStartEvent(final FSMEventType fsmEventType)
  {
    if (this.state == FSMState.Idle)
    {
      this.connectRetryCounter = 0;
      this.canAcceptConnection = true;

      try
      {
        if (this.peerConfiguration.isDampPeerOscillation() && this.fireIdleHoldTimerExpired.isJobScheduled())
        {
          return;
        }
      }
      catch (final SchedulerException e)
      {
        this.log.error("cannot query idel hold timer for peer " + this.peerConfiguration.getPeerName(), e);

        this.haveFSMError = true;
      }

      boolean temporaryPassive = false;

      if (fsmEventType == FSMEventType.AutomaticStart)
      {
        temporaryPassive = (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.lastConnectStamp) < this.peerConfiguration.getConnectRetryTime());
      }

      if (!this.peerConfiguration.isPassiveTcpEstablishment() && !temporaryPassive)
      {
        this.moveStateToConnect();
      }
      else
      {
        this.moveStateToActive();
      }

      try
      {
        if ((fsmEventType == FSMEventType.AutomaticStart) && this.peerConfiguration.isAllowAutomaticStart())
        {
          this.fireRepeatedAutomaticStart.startRepeatedJob(this.peerConfiguration.getAutomaticStartInterval());
        }
      }
      catch (final SchedulerException e)
      {
        this.log.error("failed to start automatic restart timer");

        this.haveFSMError = true;
      }

    }
  }

  /**
   * handle any kind of stop event
   */
  private void handleStopEvent(final FSMEventType type)
  {
    switch (type)
    {
      case AutomaticStop:
        this.connectRetryCounter++;
        break;
      case ManualStop:
        this.connectRetryCounter = 0;
        break;
    }

    this.moveStateToIdle();
  }

  /**
   * handle the connect retry timer fired event.
   * <ol>
   * <li>Fire disconnect remote peer callback</li>
   * <li>perform actions based on current state:
   * <ul>
   * <li>If state is <code>Connect</code>:
   * <ul>
   * <li>If peer dampening is enabled then restart the idle hold timer and move state to <code>Idle</code></li>
   * <li>If peer dampening is disabled then restart the connect retry timer then fire the connect remote peer callback and stay
   * <code>Connect</code> state</li>
   * </ul>
   * </li>
   * <li>If the state is <code>Active</code> then fire the connect remote peer callback and move the state to <code>Connect</code>.
   * <li>If the state is <code>Idle</code>:
   * <ul>
   * <li>If passive TCP estalishment is disabled then fire the connect remote peer callback and move to <code>Connect</code> state</li>
   * <li>If passive TCP estalishment is ensabled then move to <code>Connect</code> state</li>
   * </ul>
   * </li>
   * </ul>
   * </li>
   * </ol>
   */
  private void handleConnectRetryTimerExpiredEvent()
  {
    switch (this.state)
    {
      case Connect:
        this.connectedChannelManager.disconnect();

        if (this.peerConfiguration.isDampPeerOscillation())
        {
          this.state = FSMState.Idle;

          try
          {
            this.fireIdleHoldTimerExpired.scheduleJob(this.peerConfiguration.getIdleHoldTime() << this.connectRetryCounter);
          }
          catch (final SchedulerException e)
          {
            this.log.error("Interal Error: cannot schedule idle hold timer for peer " + this.peerConfiguration.getPeerName(), e);

            this.haveFSMError = true;
          }
        }
        else
        {
          this.connectRetryCounter++;

          this.moveStateToConnect();
        }
        break;
      case Active:
        this.connectRetryCounter++;

        this.moveStateToConnect();
        break;
      case Idle:
        if (!this.peerConfiguration.isPassiveTcpEstablishment())
        {
          this.moveStateToConnect();
        }
        else
        {
          this.moveStateToActive();
        }
        break;
      default:
        this.haveFSMError = true;
        break;
    }
  }

  /**
   * handle the idle hold timer expired event. If the current state is <code>Idle</code> then the machine is moved into state
   * <code>Connect</code>
   */
  private void handleHoldTimerExpiredEvent()
  {
    switch (this.state)
    {
      case Connect:
      case Active:
        this.connectRetryCounter++;
        this.moveStateToIdle();
        break;
      case OpenSent:
      case OpenConfirm:
      case Established:
        this.connectedChannelManager.fireSendHoldTimerExpiredNotification();
        this.activeChannelManager.fireSendHoldTimerExpiredNotification();

        this.connectRetryCounter++;
        this.moveStateToIdle();
        break;
      case Idle:
        // do nothing
        break;
    }
  }

  /**
   * handle the idle hold timer expired event. If the current state is <code>Idle</code> then the machine is moved into state
   * <code>Connect</code>
   */
  private void handleIdleHoldTimerExpiredEvent()
  {
    switch (this.state)
    {
      case Connect:
      case Active:
        this.connectRetryCounter++;
        this.moveStateToIdle();
        break;
      case Idle:
        this.connectRetryCounter++;
        this.moveStateToConnect();
        break;
      case OpenSent:
      case OpenConfirm:
      case Established:
        this.haveFSMError = true;
        break;
    }
  }

  /**
   * handle the delay open timer expired event. Depending on the current state the following actions are performed:
   * <ul>
   * <li>If the current state is <code>Connect</code> then send an <code>OPEN</code> message to the peer, set the hold timer to 600 seconds
   * and move the state to <code>OpenSent</code>
   * </ul>
   */
  private void handleDelayOpenTimerExpiredEvent()
  {
    switch (this.state)
    {
      case Connect:
      case Active:
        this.moveStateToOpenSent();
        break;
      case OpenSent:
      case OpenConfirm:
      case Established:
        this.haveFSMError = true;
        break;
      case Idle:
        break;
    }
  }

  /**
   * Handle TCP connections failures.
   * <ul>
   * <li>if the current state is <code>Connect</code> then move to <code>Active</code>
   * <li>if the current state is <code>Active</code> then move to <code>Idle</code>
   * </ul>
   *
   * @param i
   */
  private void handleTcpConnectionFails(final FSMChannel channel)
  {
    switch (this.state)
    {
      case Connect:
        try
        {
          if (this.isDelayOpenTimerRunning())
          {
            this.moveStateToActive();
          }
          else
          {
            this.moveStateToIdle();
          }
        }
        catch (final SchedulerException e)
        {
          this.log.error("Internal Error: Failed to query delay open timer for peer " + this.peerConfiguration.getPeerName());

          this.haveFSMError = true;
        }
        break;
      case Active:
        this.connectRetryCounter++;
        this.moveStateToIdle();
        break;
      case OpenSent:
        if (this.connectedChannelManager.isManagedChannel(channel))
        {
          if (!this.activeChannelManager.hasSeenOutbboundFSMEvent(FSMEventType.BGPOpen))
          {
            this.moveStateToActive();
          }
          this.connectedChannelManager.clear();
        }
        else if (this.activeChannelManager.isManagedChannel(channel))
        {
          if (!this.connectedChannelManager.hasSeenOutbboundFSMEvent(FSMEventType.BGPOpen))
          {
            if (this.connectedChannelManager.isConnected())
            {
              this.moveStateToConnect();
            }
            else
            {
              this.moveStateToActive();
            }
          }
          this.activeChannelManager.clear();
        }
        break;
      case OpenConfirm:
        if (this.connectedChannelManager.isManagedChannel(channel))
        {
          this.connectedChannelManager.clear();
          if (!this.activeChannelManager.hasSeenInboundFSMEvent(FSMEventType.BGPOpen))
          {
            this.moveStateToIdle();
          }
        }
        else if (this.activeChannelManager.isManagedChannel(channel))
        {
          this.activeChannelManager.clear();
          if (!this.connectedChannelManager.hasSeenInboundFSMEvent(FSMEventType.BGPOpen))
          {
            this.moveStateToIdle();
          }
        }
        break;
      case Established:
        if (this.connectedChannelManager.isManagedChannel(channel))
        {
          if (this.connectedChannelManager.hasSeenInboundFSMEvent(FSMEventType.BGPOpen))
          {
            this.moveStateToIdle();
          }
          else
          {
            this.connectedChannelManager.clear();
          }
        }
        else if (this.activeChannelManager.isManagedChannel(channel))
        {
          if (this.activeChannelManager.hasSeenOutbboundFSMEvent(FSMEventType.BGPOpen))
          {
            this.moveStateToIdle();
          }
          else
          {
            this.activeChannelManager.clear();
          }
        }
        break;
      case Idle:
        // do nothing
        break;
    }
  }

  /**
   * Handle the connection originated by the local peer to the remote peer has being established.
   * <ul>
   * <li>If the current state is <code>Connect</code>:
   * <ul>
   * <li>If the delay open flag is set then the connect retry timer is canceled, the delay open timer is started with the configured value.
   * The state stay at <code>Connect</code></li>
   * <li>If the delay open flag is not set then the connect retry timer is canceled, an <code>OPEN</code> message is sent to the peer and
   * the state is moved to <code>OpenSent</code></li>
   * </ul>
   * </li>
   * </ul>
   *
   * @param channelId
   *          the ID of the channel with which the connection was established
   */
  private void handleTcpConnectionAcked(final FSMChannel channel)
  {
    switch (this.state)
    {
      case Connect:
      case Active:
        this.connectedChannelManager.connect(channel);
        if (this.peerConfiguration.isDelayOpen())
        {
          try
          {
            this.fireConnectRetryTimeExpired.cancelJob();
          }
          catch (final SchedulerException e)
          {
            this.log.error("Internal Error: cannot cancel connect retry timer for peer " + this.peerConfiguration.getPeerName(), e);
          }

          try
          {
            this.fireDelayOpenTimerExpired.cancelJob();
            this.fireDelayOpenTimerExpired.scheduleJob(this.peerConfiguration.getDelayOpenTime());
          }
          catch (final SchedulerException e)
          {
            this.log.error("Internal Error: cannot schedule open delay timer for peer " + this.peerConfiguration.getPeerName(), e);

            this.haveFSMError = true;
          }
        }
        else
        {
          this.moveStateToOpenSent();
        }
        break;
      case OpenSent:
      case OpenConfirm:
      case Established:
        if (this.connectedChannelManager.isConnected() || !this.activeChannelManager.isConnected())
        {
          this.haveFSMError = true;
        }
        else
        {
          this.connectedChannelManager.connect(channel);
        }
        break;
      case Idle:
        // do nothing
        break;
    }
  }

  /**
   * Handle the connection from the remote peer to the local peer being established.
   * <ul>
   * <li>If the current state is <code>Connect</code>:
   * <ul>
   * <li>If the delay open flag is set then the connect retry timer is canceled, the delay open timer is started with the configured value.
   * The state stay at <code>Connect</code></li>
   * <li>If the delay open flag is not set then the connect retry timer is canceled, an <code>OPEN</code> message is sent to the peer and
   * the state is moved to <code>OpenSent</code></li>
   * </ul>
   * </li>
   * </ul>
   *
   * @param channelId
   *          the ID of the channel with which the connection was established
   */
  private void handleTcpConnectionConfirmed(final FSMChannel channel)
  {
    switch (this.state)
    {
      case Connect:
      case Active:
        this.activeChannelManager.connect(channel);

        if (this.peerConfiguration.isDelayOpen())
        {
          try
          {
            this.fireConnectRetryTimeExpired.cancelJob();
          }
          catch (final SchedulerException e)
          {
            this.log.error("Internal Error: cannot cancel connect retry timer for peer " + this.peerConfiguration.getPeerName(), e);
          }

          try
          {
            this.fireDelayOpenTimerExpired.cancelJob();
            this.fireDelayOpenTimerExpired.scheduleJob(this.peerConfiguration.getDelayOpenTime());
          }
          catch (final SchedulerException e)
          {
            this.log.error("Internal Error: cannot schedule open delay timer for peer " + this.peerConfiguration.getPeerName(), e);

            this.haveFSMError = true;
          }
        }
        else
        {
          this.moveStateToOpenSent();
        }
        break;
      case OpenSent:
      case OpenConfirm:
      case Established:
        if (this.activeChannelManager.isConnected() && this.activeChannelManager.hasSeenInboundFSMEvent(FSMEventType.BGPOpen))
        {
          this.haveFSMError = true;
        }
        else
        {
          this.activeChannelManager.connect(channel);
        }
        break;
      case Idle:
        // do nothing
        break;
    }
  }

  /**
   * handle an inbound <code>OPEN</code> mesage from the remote peer
   *
   * @param channelId
   *          the ID of the channel with which the connection was established
   */
  private void handleBgpOpenEvent(final FSMChannel channel, final InternalFSMChannelManager channelManager)
  {
    switch (this.state)
    {
      case Connect:
      case Active:
        try
        {
          if (this.fireDelayOpenTimerExpired.isJobScheduled())
          {
            this.moveStateToOpenConfirm(true);
          }
          else
          {
            this.connectRetryCounter++;
            this.moveStateToIdle();
          }
        }
        catch (final SchedulerException e)
        {
          this.log.error("cannot query delay openn timer for peer " + this.peerConfiguration.getPeerName(), e);

          this.haveFSMError = true;
        }
        break;
      case OpenSent:
        if (channelManager.hasSeenInboundFSMEvent(FSMEventType.BGPOpen))
        {
          this.haveFSMError = true;
        }
        else if (this.connectedChannelManager.isConnected() && this.activeChannelManager.isConnected())
        {
          if (this.peerConfiguration.getLocalBgpIdentifier() < this.peerConfiguration.getRemoteBgpIdentifier())
          {
            this.connectedChannelManager.fireSendCeaseNotification();
            this.connectedChannelManager.disconnect();
          }
          else
          {
            this.activeChannelManager.fireSendCeaseNotification();
            this.activeChannelManager.disconnect();
          }
        }
        this.moveStateToOpenConfirm(false);
        break;
      case OpenConfirm:
        if (channelManager.hasSeenInboundFSMEvent(FSMEventType.BGPOpen))
        {
          this.haveFSMError = true;
        }
        else if (this.connectedChannelManager.isConnected() && this.activeChannelManager.isConnected())
        {
          if (this.peerConfiguration.getLocalBgpIdentifier() < this.peerConfiguration.getRemoteBgpIdentifier())
          {
            this.connectedChannelManager.fireSendCeaseNotification();
            this.connectedChannelManager.disconnect();
          }
          else
          {
            this.activeChannelManager.fireSendCeaseNotification();
            this.activeChannelManager.disconnect();
          }
        }
        break;
      case Established:
        if (channelManager.hasSeenInboundFSMEvent(FSMEventType.BGPOpen))
        {
          this.haveFSMError = true;
        }
        else if (this.connectedChannelManager.isConnected() && this.activeChannelManager.isConnected())
        {
          if (this.peerConfiguration.getLocalBgpIdentifier() < this.peerConfiguration.getRemoteBgpIdentifier())
          {
            this.connectedChannelManager.fireSendCeaseNotification();
            this.moveStateToIdle();
          }
          else
          {
            this.activeChannelManager.fireSendCeaseNotification();
            this.activeChannelManager.disconnect();
          }
        }
        break;
      case Idle:
        // do nothing here
        break;
    }
  }

  /**
   * handle an <code>KEEPALIVE</CODE> message sent from the remote peer
   */
  private void handleKeepaliveMessageEvent()
  {
    switch (this.state)
    {
      case Connect:
      case Active:
        this.connectRetryCounter++;
        this.moveStateToIdle();
        break;
      case OpenSent:
        this.haveFSMError = true;
        break;
      case OpenConfirm:
        this.moveStateToEstablished();
        break;
      case Established:
        try
        {
          this.fireHoldTimerExpired.cancelJob();
          this.fireHoldTimerExpired.scheduleJob(this.getNegotiatedHoldTime());
        }
        catch (final SchedulerException e)
        {
          this.log.error("Interal Error: cannot schedule connect retry timer for peer " + this.peerConfiguration.getPeerName(), e);

          this.haveFSMError = true;
        }
        break;
      case Idle:
        // do nothing
        break;
    }
  }

  /**
   * handle the expired keepalive timer on the local side
   */
  private void handleKeepaliveTimerExpiresEvent()
  {
    switch (this.state)
    {
      case Connect:
      case Active:
        this.connectRetryCounter++;
        this.moveStateToIdle();
        break;
      case OpenSent:
        this.haveFSMError = true;
        break;
      case OpenConfirm:
      case Established:
        if (this.activeChannelManager.hasSeenInboundFSMEvent(FSMEventType.BGPOpen))
        {
          this.activeChannelManager.fireSendKeepaliveMessage();
        }
        if (this.connectedChannelManager.hasSeenInboundFSMEvent(FSMEventType.BGPOpen))
        {
          this.connectedChannelManager.fireSendKeepaliveMessage();
        }

        try
        {
          this.fireKeepaliveTimerExpired.scheduleJob(this.getSendKeepaliveTime());
        }
        catch (final SchedulerException e)
        {
          this.log.error("cannont start send keepalive timer", e);

          this.haveFSMError = true;
        }
        break;
      case Idle:
        // do nothing
        break;
    }
  }

  /**
   * handle a <code>NOTIFY</code> message sent from the remote peer
   */
  private void handleNotifyMessageEvent()
  {
    switch (this.state)
    {
      case Connect:
      case Active:
        this.connectRetryCounter++;
        this.moveStateToIdle();
        break;
      case OpenSent:
      case OpenConfirm:
      case Established:
        this.haveFSMError = true;
        break;
      case Idle:
        // do nothing
        break;
    }
  }

  /**
   * handle a malformed <code>NOTIFY</code> message sent from the remote peer
   */
  private void handleNotifyMessageVersionErrorEvent()
  {
    switch (this.state)
    {
      case Connect:
      case Active:
      case OpenSent:
      case OpenConfirm:
      case Established:
        this.moveStateToIdle();
        break;
      case Idle:
        // do nothing
        break;
    }
  }

  /**
   * handle a malformed <code>OPEN</code> message sent from the remote peer
   */
  private void handleBGPOpenMessageErrorEvent()
  {
    switch (this.state)
    {
      case Connect:
      case Active:
      case OpenSent:
      case OpenConfirm:
      case Established:
        this.connectRetryCounter++;
        this.moveStateToIdle();
        break;
      case Idle:
        // do nothing
        break;
    }
  }

  /**
   * handle a malformed BGP packet where the initial header checks failed
   */
  private void handleBGPHeaderErrorEvent()
  {
    switch (this.state)
    {
      case Connect:
      case Active:
      case OpenSent:
      case OpenConfirm:
      case Established:
        this.connectRetryCounter++;
        this.moveStateToIdle();
        break;
      case Idle:
        // do nothing
        break;
    }
  }

  /**
   * handle an <code>UPDATE</code> message sent from the remote peer
   */
  private void handleUpdateMessageEvent()
  {
    switch (this.state)
    {
      case Connect:
      case Active:
        this.connectRetryCounter++;
        this.moveStateToIdle();
        break;
      case OpenSent:
      case OpenConfirm:
        this.haveFSMError = true;
        break;
      case Established:
        try
        {
          this.fireHoldTimerExpired.cancelJob();
          this.fireHoldTimerExpired.scheduleJob(this.getNegotiatedHoldTime());
        }
        catch (final SchedulerException e)
        {
          this.log.error("Interal Error: cannot schedule connect retry timer for peer " + this.peerConfiguration.getPeerName(), e);

          this.haveFSMError = true;
        }
        break;
      case Idle:
        // do nothing
        break;
    }
  }

  /**
   * handle a malformed <code>UPDATE</code> message sent from the remote peer
   */
  private void handleUpdateMessageErrorEvent()
  {
    switch (this.state)
    {
      case Connect:
      case Active:
        this.connectRetryCounter++;
        this.moveStateToIdle();
        break;
      case OpenSent:
      case OpenConfirm:
        this.haveFSMError = true;
        break;
      case Established:
        this.activeChannelManager.fireSendUpdateErrorNotification();
        this.connectedChannelManager.fireSendUpdateErrorNotification();
        this.connectRetryCounter++;
        this.moveStateToIdle();
        break;
      case Idle:
        // do nothing
        break;
    }
  }

  /**
   * check if connections can be accepted
   *
   * @return
   */
  boolean isCanAcceptConnection()
  {
    return this.canAcceptConnection;
  }

  /**
   * @return the state
   */
  FSMState getState()
  {
    return this.state;
  }

  /**
   * @return the connectRetryCounter
   */
  int getConnectRetryCounter()
  {
    return this.connectRetryCounter;
  }

  /**
   * check if the connect retry timer is currently running
   *
   * @return true if the timer is running
   * @throws SchedulerException
   */
  boolean isConnectRetryTimerRunning() throws SchedulerException
  {
    return this.fireConnectRetryTimeExpired.isJobScheduled();
  }

  /**
   * get the date when the connect retry timer will fire
   *
   * @return the date when the timmer will fire
   * @throws SchedulerException
   */
  Date getConnectRetryTimerDueWhen() throws SchedulerException
  {
    return this.fireConnectRetryTimeExpired.getFiredWhen();
  }

  /**
   * check if the idle hold timer is currently running
   *
   * @return
   * @throws SchedulerException
   */
  boolean isIdleHoldTimerRunning() throws SchedulerException
  {
    return this.fireIdleHoldTimerExpired.isJobScheduled();
  }

  /**
   * get the date when then idle hold timer will fire
   *
   * @return
   * @throws SchedulerException
   */
  Date getIdleHoldTimerDueWhen() throws SchedulerException
  {
    return this.fireIdleHoldTimerExpired.getFiredWhen();
  }

  /**
   * check if the delay open timer is currently running
   *
   * @return
   * @throws SchedulerException
   */
  boolean isDelayOpenTimerRunning() throws SchedulerException
  {
    return this.fireDelayOpenTimerExpired.isJobScheduled();
  }

  /**
   * get the date when the delay open timer will fire
   *
   * @return
   * @throws SchedulerException
   */
  public Date getDelayOpenTimerDueWhen() throws SchedulerException
  {
    return this.fireDelayOpenTimerExpired.getFiredWhen();
  }

  /**
   * Check if the hold timer is running
   *
   * @return
   * @throws SchedulerException
   */
  boolean isHoldTimerRunning() throws SchedulerException
  {
    return this.fireHoldTimerExpired.isJobScheduled();
  }

  /**
   * get the date when the hold timer will fire.
   *
   * @return
   * @throws SchedulerException
   */
  Date getHoldTimerDueWhen() throws SchedulerException
  {
    return this.fireHoldTimerExpired.getFiredWhen();
  }

  /**
   * check if the send keeplives timer is running
   *
   * @return
   * @throws SchedulerException
   */
  public boolean isKeepaliveTimerRunning() throws SchedulerException
  {
    return this.fireKeepaliveTimerExpired.isJobScheduled();
  }

  /**
   * get the date when the next keepalive packket is to be sent
   *
   * @return
   * @throws SchedulerException
   */
  public Date getKeepaliveTimerDueWhen() throws SchedulerException
  {
    return this.fireKeepaliveTimerExpired.getFiredWhen();
  }

  /**
   * Check if the automatic start event generator is running
   *
   */
  boolean isAutomaticStartRunning() throws SchedulerException
  {
    return this.fireRepeatedAutomaticStart.isJobScheduled();
  }

  /**
   * get the date the automatic start timer will fire the next time.
   *
   * @return
   * @throws SchedulerException
   */
  Date getAutomaticStartDueWhen() throws SchedulerException
  {
    return this.fireRepeatedAutomaticStart.getNextFireWhen();
  }

  /**
   * @return the proposedHoldTimer
   */
  int getPeerProposedHoldTime()
  {
    return this.peerProposedHoldTime;
  }

  /**
   * @param proposedHoldTimer
   *          the proposedHoldTimer to set
   */
  void setPeerProposedHoldTime(final int proposedHoldTime)
  {
    this.peerProposedHoldTime = proposedHoldTime;
  }

  /**
   * get the negotiated hold time. This is the minimum of the locally configured hold time and the hold time value received from the remote
   * peer in the initial open packet. It is assured that the negotiated hold time cannot be less than 3 seconds as specified by RFC4271.
   *
   * @return
   */
  int getNegotiatedHoldTime()
  {
    int negotiatedHoldTime = Math.min(this.peerConfiguration.getHoldTime(), this.peerProposedHoldTime);

    if (negotiatedHoldTime < 3)
    {
      negotiatedHoldTime = 3;
    }

    return negotiatedHoldTime;
  }

  /**
   * get the keepalive interval which is 1/3 of the negotiated hold time. It is assured that the interval cannot be less than 1 second as
   * specified by RFC4271
   *
   * @return
   */
  private int getSendKeepaliveTime()
  {
    return Math.max(this.getNegotiatedHoldTime() / 3, 1);
  }

  /**
   * Move from any other state to <code>Connect</code> state. It performs the following actions:
   * <ol>
   * <li>cancel the idle hold timer</li>
   * <li>cancel the connect retry timer</li>
   * <li>restart the connect retry timer with the configured value</li>
   * <li>fire the connect to remote peer callback</li>
   * <li>set the state to <code>Connect</code></li>
   * </ol>
   */
  private void moveStateToConnect()
  {
    
    try
    {
      this.fireHoldTimerExpired.cancelJob();
      this.fireIdleHoldTimerExpired.cancelJob();
      this.fireConnectRetryTimeExpired.cancelJob();
      this.fireConnectRetryTimeExpired.scheduleJob(this.peerConfiguration.getConnectRetryTime());
    }
    catch (final SchedulerException e)
    {
      log.error("Interal Error: cannot schedule connect retry timer for peer " + this.peerConfiguration.getPeerName(), e);
      this.haveFSMError = true;
    }

    this.callbacks.fireConnectRemotePeer();
    this.lastConnectStamp = System.currentTimeMillis();

    this.state = FSMState.Connect;
    
    log.info("FSM for peer {} moved to {}", this.peerConfiguration.getPeerName(), this.state);
    
  }

  /**
   * Move from any other state to <code>Active</code> state. It performs the following actions:
   * <ol>
   * <li>cancel the idle hold timer</li>
   * <li>cancel the connect retry timer</li>
   * <li>cancel the delay open timer</li>
   * <li>cancal the hold timer</li>
   * <li>restart the connect retry timer with the configured value</li>
   * <li>fire the connect to remote peer callback</li>
   * <li>set the state to <code>Active</code></li>
   * </ol>
   */
  private void moveStateToActive()
  {
    try
    {
      this.fireIdleHoldTimerExpired.cancelJob();
      this.fireConnectRetryTimeExpired.cancelJob();
      this.fireDelayOpenTimerExpired.cancelJob();
      this.fireHoldTimerExpired.cancelJob();

      this.fireConnectRetryTimeExpired.scheduleJob(this.peerConfiguration.getConnectRetryTime());
    }
    catch (final SchedulerException e)
    {
      this.log.error("Interal Error: cannot schedule connect retry timer for peer " + this.peerConfiguration.getPeerName(), e);

      this.haveFSMError = true;
    }

    this.state = FSMState.Active;
    this.log.info("FSM for peer " + this.peerConfiguration.getPeerName() + " moved to " + this.state);
  }

  /**
   * Move from any other state to <code>Idle</code> state. It performs the following actions:
   * <ol>
   * <li>cancel the idle hold timer</li>
   * <li>cancel the connect retry timer</li>
   * <li>cancel the delay open timer</li>
   * <li>cancel the hold timer</li>
   * <li>cancel the send keepalive timer</li>
   * <li>release all BGP resources</li>
   * <li>disconnect the remote peer</li>
   * <li>restart the connect retry timer with the configured value if peer dampening is disabled</li>
   * <li>restart the idle hold timer with the configured value if peer dampening is enabled</li>
   * <li>set the state to <code>Idle</code></li>
   * </ol>
   */
  private void moveStateToIdle()
  {
    try
    {
      this.fireIdleHoldTimerExpired.cancelJob();
      this.fireConnectRetryTimeExpired.cancelJob();
      this.fireDelayOpenTimerExpired.cancelJob();
      this.fireHoldTimerExpired.cancelJob();
      this.fireKeepaliveTimerExpired.cancelJob();
    }
    catch (final SchedulerException e)
    {
      this.log.error("Interal Error: cannot cancel timers for peer " + this.peerConfiguration.getPeerName(), e);

      this.haveFSMError = true;
    }

    this.callbacks.fireReleaseBGPResources();
    this.activeChannelManager.disconnect();
    this.connectedChannelManager.disconnect();

    if (this.peerConfiguration.isDampPeerOscillation())
    {
      try
      {
        this.fireIdleHoldTimerExpired.scheduleJob(this.peerConfiguration.getIdleHoldTime() << this.connectRetryCounter);
      }
      catch (final SchedulerException e)
      {
        this.log.error("Interal Error: cannot schedule idle hold timer for peer " + this.peerConfiguration.getPeerName(), e);

        this.haveFSMError = true;
      }
    }
    this.state = FSMState.Idle;
    this.log.info("FSM for peer " + this.peerConfiguration.getPeerName() + " moved to " + this.state);
  }

  /**
   * Move from any other state to <code>OpenSent</code> state. It performs the following actions:
   * <ol>
   * <li>cancel the idle hold timer</li>
   * <li>cancel the connect retry timer</li>
   * <li>start the hold timer with 600 seconds</li>
   * <li>fire the send <code>OPEN</code> message to remote peer callback</li>
   * <li>set the state to <code>OpenSent</code></li>
   * </ol>
   */
  private void moveStateToOpenSent()
  {
    try
    {
      this.fireIdleHoldTimerExpired.cancelJob();
      this.fireConnectRetryTimeExpired.cancelJob();

      this.fireHoldTimerExpired.scheduleJob(600);
    }
    catch (final SchedulerException e)
    {
      this.log.error("Interal Error: cannot schedule connect retry timer for peer " + this.peerConfiguration.getPeerName(), e);

      this.haveFSMError = true;
    }

    this.callbacks.fireCompleteBGPLocalInitialization();
    this.connectedChannelManager.fireSendOpenMessage();
    this.activeChannelManager.fireSendOpenMessage();

    this.state = FSMState.OpenSent;
    this.log.info("FSM for peer " + this.peerConfiguration.getPeerName() + " moved to " + this.state);
  }

  /**
   * Move from any other state to <code>OpenSent</code> state. It performs the following actions:
   * <ol>
   * <li>cancel the idle hold timer</li>
   * <li>cancel the connect retry timer</li>
   * <li>start the hold timer with 600 seconds</li>
   * <li>fire the send <code>OPEN</code> message to remote peer callback</li>
   * <li>set the state to <code>OpenSent</code></li>
   * </ol>
   */
  private void moveStateToEstablished()
  {
    try
    {
      this.fireIdleHoldTimerExpired.cancelJob();
      this.fireConnectRetryTimeExpired.cancelJob();

      this.fireHoldTimerExpired.cancelJob();
      this.fireHoldTimerExpired.scheduleJob(this.getNegotiatedHoldTime());
    }
    catch (final SchedulerException e)
    {
      this.log.error("Interal Error: cannot schedule connect retry timer for peer " + this.peerConfiguration.getPeerName(), e);

      this.haveFSMError = true;
    }

    if (!this.activeChannelManager.hasSeenOutbboundFSMEvent(FSMEventType.KeepAliveMsg))
    {
      this.activeChannelManager.disconnect();
    }
    if (!this.connectedChannelManager.hasSeenOutbboundFSMEvent(FSMEventType.KeepAliveMsg))
    {
      this.connectedChannelManager.disconnect();
    }

    this.state = FSMState.Established;
    this.log.info("FSM for peer " + this.peerConfiguration.getPeerName() + " moved to " + this.state);

    this.callbacks.fireEstablished();
  }

  /**
   * move the state to open confirm.
   * <ul>
   * <li>If called from the states <code>CONNECT</code> or <code>ACTIVE</code> then complete BGP initialization and send the peer an
   * <code>OPEN</code> message</li>
   * <li>If called from state <code>OPEN SENT</code> then do <b>not</b> complete BGP initialization and send the peer an <code>OPEN</code>
   * message</li>
   * </ul>
   *
   * @param sendOpenMessage
   */
  private void moveStateToOpenConfirm(final boolean sendOpenMessage)
  {
    if (sendOpenMessage)
    {
      this.callbacks.fireCompleteBGPLocalInitialization();
      this.activeChannelManager.fireSendOpenMessage();
      this.connectedChannelManager.fireSendOpenMessage();
    }

    this.callbacks.fireCompleteBGPPeerInitialization();

    this.activeChannelManager.fireSendKeepaliveMessage();
    this.connectedChannelManager.fireSendKeepaliveMessage();

    try
    {
      this.fireConnectRetryTimeExpired.cancelJob();
    }
    catch (final SchedulerException e)
    {
      this.log.error("cannont cancel connect retry timer", e);

      this.haveFSMError = true;
    }
    try
    {
      this.fireDelayOpenTimerExpired.cancelJob();
    }
    catch (final SchedulerException e)
    {
      this.log.error("cannont cancel open delay timer", e);

      this.haveFSMError = true;
    }

    if (!this.peerConfiguration.isHoldTimerDisabled())
    {
      try
      {
        this.fireKeepaliveTimerExpired.scheduleJob(this.getSendKeepaliveTime());
      }
      catch (final SchedulerException e)
      {
        this.log.error("cannont start send keepalive timer", e);

        this.haveFSMError = true;
      }

      try
      {
        this.fireHoldTimerExpired.cancelJob();
        this.fireHoldTimerExpired.scheduleJob(this.getNegotiatedHoldTime());
      }
      catch (final SchedulerException e)
      {

      }
    }
    this.state = FSMState.OpenConfirm;
    this.log.info("FSM for peer " + this.peerConfiguration.getPeerName() + " moved to " + this.state);
  }

  public void flagFSMError()
  {
    this.haveFSMError = true;
  }
}
