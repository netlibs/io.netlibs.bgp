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
 */
package com.jive.oss.bgp.netty.fsm;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import com.jive.oss.bgp.config.nodes.PeerConfiguration;
import com.jive.oss.bgp.net.ASType;
import com.jive.oss.bgp.net.AddressFamily;
import com.jive.oss.bgp.net.AddressFamilyKey;
import com.jive.oss.bgp.net.RIBSide;
import com.jive.oss.bgp.net.SubsequentAddressFamily;
import com.jive.oss.bgp.net.attributes.MultiProtocolReachableNLRI;
import com.jive.oss.bgp.net.attributes.MultiProtocolUnreachableNLRI;
import com.jive.oss.bgp.net.attributes.NextHopPathAttribute;
import com.jive.oss.bgp.net.attributes.PathAttribute;
import com.jive.oss.bgp.net.capabilities.Capability;
import com.jive.oss.bgp.net.capabilities.MultiProtocolCapability;
import com.jive.oss.bgp.netty.BGPv4Constants;
import com.jive.oss.bgp.netty.FSMState;
import com.jive.oss.bgp.netty.PeerConnectionInformation;
import com.jive.oss.bgp.netty.handlers.BgpEvent;
import com.jive.oss.bgp.netty.handlers.NotificationEvent;
import com.jive.oss.bgp.netty.protocol.BGPv4Packet;
import com.jive.oss.bgp.netty.protocol.FiniteStateMachineErrorNotificationPacket;
import com.jive.oss.bgp.netty.protocol.HoldTimerExpiredNotificationPacket;
import com.jive.oss.bgp.netty.protocol.KeepalivePacket;
import com.jive.oss.bgp.netty.protocol.NotificationPacket;
import com.jive.oss.bgp.netty.protocol.UnspecifiedCeaseNotificationPacket;
import com.jive.oss.bgp.netty.protocol.open.OpenNotificationPacket;
import com.jive.oss.bgp.netty.protocol.open.OpenPacket;
import com.jive.oss.bgp.netty.protocol.open.UnsupportedVersionNumberNotificationPacket;
import com.jive.oss.bgp.netty.protocol.update.InvalidNextHopException;
import com.jive.oss.bgp.netty.protocol.update.UpdateNotificationPacket;
import com.jive.oss.bgp.netty.protocol.update.UpdatePacket;
import com.jive.oss.bgp.netty.service.BGPv4Client;
import com.jive.oss.bgp.rib.PeerRoutingInformationBase;
import com.jive.oss.bgp.rib.PeerRoutingInformationBaseManager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@Slf4j
public class BGPv4FSM
{

  private class FSMChannelImpl implements FSMChannel
  {

    private final Channel channel;

    public FSMChannelImpl(final Channel channel)
    {
      this.channel = channel;
    }

    /**
     * @return the channel
     */
    private Channel getChannel()
    {
      return this.channel;
    }

  }

  /**
   * Internal proxy class to expose the peer connection information to interested handlers
   *
   * @author Rainer Bieniek (Rainer.Bieniek@web.de)
   *
   */
  private class PeerConnectionInformationImpl implements PeerConnectionInformation
  {

    @Override
    public ASType getAsTypeInUse()
    {
      return BGPv4FSM.this.asTypeInUse;
    }

    /**
     *
     * @return
     */
    @Override
    public int getLocalAS()
    {
      return BGPv4FSM.this.peerConfig.getLocalAS();
    }

    /**
     *
     * @return
     */
    @Override
    public int getRemoteAS()
    {
      return BGPv4FSM.this.peerConfig.getRemoteAS();
    }

    /**
     * Test if the connection describes an IBGP connection (peers in the same AS)
     *
     * @return <code>true</code> if IBGP connection, <code>false</code> otherwise
     */
    @Override
    public boolean isIBGPConnection()
    {
      return (this.getRemoteAS() == this.getLocalAS());
    }

    /**
     * Test if the connection describes an EBGP connection (peers in the same AS)
     *
     * @return <code>true</code> if EBGP connection, <code>false</code> otherwise
     */
    @Override
    public boolean isEBGPConnection()
    {
      return (this.getRemoteAS() != this.getLocalAS());
    }

    /**
     * Test if this connection uses 4 octet AS numbers
     *
     * @return
     */
    @Override
    public boolean isAS4OctetsInUse()
    {
      return (BGPv4FSM.this.asTypeInUse == ASType.AS_NUMBER_4OCTETS);
    }

    /**
     * @return the localBgpIdentifier
     */
    @Override
    public long getLocalBgpIdentifier()
    {
      return BGPv4FSM.this.peerConfig.getLocalBgpIdentifier();
    }

    /**
     * @return the remoteBgpIdentifier
     */
    @Override
    public long getRemoteBgpIdentifier()
    {
      return BGPv4FSM.this.peerConfig.getRemoteBgpIdentifier();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
      final StringBuilder builder = new StringBuilder();
      builder.append("PeerConnectionInformation [localAS=").append(this.getLocalAS())
          .append(", remoteAS=").append(this.getRemoteAS())
          .append(", localBgpIdentifier=").append(this.getLocalBgpIdentifier())
          .append(", remoteBgpIdentifier=").append(this.getRemoteBgpIdentifier())
          .append(", ");
      if (this.getAsTypeInUse() != null)
      {
        builder.append("asTypeInUse=").append(this.getAsTypeInUse());
      }
      builder.append("]");
      return builder.toString();
    }
  }

  /**
   * Internal class to bind callbacks from the internal state machine to concrete actions
   *
   * @author Rainer Bieniek (Rainer.Bieniek@web.de)
   *
   */
  private class InternalFSMCallbacksImpl implements InternalFSMCallbacks
  {

    @Override
    public void fireConnectRemotePeer()
    {
      final BGPv4Client client = BGPv4FSM.this.clientProvider;
      BGPv4FSM.this.managedChannels.add(new FSMChannelImpl(client.startClient(BGPv4FSM.this.peerConfig).channel()));
    }

    @Override
    public void fireDisconnectRemotePeer(final FSMChannel channel)
    {
      if (BGPv4FSM.this.managedChannels.contains(channel))
      {
        ((FSMChannelImpl) channel).getChannel().close();
        BGPv4FSM.this.managedChannels.remove(channel);
      }

    }

    @Override
    public void fireSendOpenMessage(final FSMChannel channel)
    {
      if (BGPv4FSM.this.managedChannels.contains(channel))
      {

        final OpenPacket packet = new OpenPacket();

        packet.setAutonomousSystem(BGPv4FSM.this.peerConfig.getLocalAS());
        packet.setBgpIdentifier(BGPv4FSM.this.peerConfig.getLocalBgpIdentifier());
        packet.setHoldTime(BGPv4FSM.this.peerConfig.getHoldTime());
        packet.setProtocolVersion(BGPv4Constants.BGP_VERSION);

        BGPv4FSM.this.capabilitiesNegotiator.insertLocalCapabilities(packet);

        ((FSMChannelImpl) channel).getChannel().write(packet);

      }
    }

    @Override
    public void fireSendInternalErrorNotification(final FSMChannel channel)
    {
      if (BGPv4FSM.this.managedChannels.contains(channel))
      {
        ((FSMChannelImpl) channel).getChannel().write(new FiniteStateMachineErrorNotificationPacket());
      }
    }

    @Override
    public void fireSendCeaseNotification(final FSMChannel channel)
    {
      if (BGPv4FSM.this.managedChannels.contains(channel))
      {
        ((FSMChannelImpl) channel).getChannel().write(new UnspecifiedCeaseNotificationPacket());
      }
    }

    @Override
    public void fireSendKeepaliveMessage(final FSMChannel channel)
    {
      if (BGPv4FSM.this.managedChannels.contains(channel))
      {
        ((FSMChannelImpl) channel).getChannel().write(new KeepalivePacket());
      }
    }

    @Override
    public void fireReleaseBGPResources()
    {

      if (BGPv4FSM.this.prib != null)
      {
        BGPv4FSM.this.prib.destroyAllRoutingInformationBases();
      }
      BGPv4FSM.this.prib = null;

      try
      {
        BGPv4FSM.this.oruq.shutdown();
      }
      catch (final SchedulerException e)
      {
        BGPv4FSM.log.error("failed to shutdown UPDATE send trigger", e);
      }

    }

    @Override
    public void fireCompleteBGPLocalInitialization()
    {
      BGPv4FSM.this.prib = BGPv4FSM.this.pribManager.peerRoutingInformationBase(BGPv4FSM.this.peerConfig.getPeerName());

      // allocate all RIBs for the local end which are configured
      for (final MultiProtocolCapability mpcap : BGPv4FSM.this.capabilitiesNegotiator.listLocalCapabilities(MultiProtocolCapability.class))
      {
        BGPv4FSM.this.prib.allocateRoutingInformationBase(RIBSide.Local, mpcap.toAddressFamilyKey());
      }

      BGPv4FSM.this.prib.addRoutingListener(BGPv4FSM.this.oruq);
    }

    @Override
    public void fireCompleteBGPPeerInitialization()
    {

      // allocate all RIBs for the local end which are configured
      for (final MultiProtocolCapability mpcap : BGPv4FSM.this.capabilitiesNegotiator.listRemoteCapabilities(MultiProtocolCapability.class))
      {
        System.err.println(mpcap.toAddressFamilyKey());
        BGPv4FSM.this.prib.allocateRoutingInformationBase(RIBSide.Remote, mpcap.toAddressFamilyKey());
      }

      // build a routing update filter for outbound routing updates
      for (final MultiProtocolCapability mpcap : BGPv4FSM.this.capabilitiesNegotiator.intersectLocalAndRemoteCapabilities(MultiProtocolCapability.class))
      {
        BGPv4FSM.this.outboundAddressFamilyMask.add(mpcap.toAddressFamilyKey());
      }

      BGPv4FSM.this.oruq.setUpdateMask(BGPv4FSM.this.outboundAddressFamilyMask);
    }

    @Override
    public void fireSendHoldTimerExpiredNotification(final FSMChannel channel)
    {
      if (BGPv4FSM.this.managedChannels.contains(channel))
      {
        ((FSMChannelImpl) channel).getChannel().write(new HoldTimerExpiredNotificationPacket());
      }
    }

    @Override
    public void fireSendUpdateErrorNotification(final FSMChannel channel)
    {
    }

    @Override
    public void fireEstablished()
    {

      BGPv4FSM.this.prib.visitRoutingBases(RIBSide.Local, BGPv4FSM.this.oruq.getImportVisitor(), BGPv4FSM.this.outboundAddressFamilyMask);

      try
      {
        BGPv4FSM.this.oruq.startSendingUpdates(BGPv4FSM.this.internalFsm.getNegotiatedHoldTime() / 3);
      }
      catch (final SchedulerException e)
      {
        BGPv4FSM.log.error("failed to start UPDATE send trigger", e);

        BGPv4FSM.this.internalFsm.flagFSMError();
      }
    }

  }

  private class SendLocalRoutingUpdateCallback implements OutboundRoutingUpdateCallback, ChannelFutureListener
  {

    private final List<UpdatePacket> updates = new LinkedList<UpdatePacket>();

    @Override
    public void sendUpdates(final List<UpdatePacket> updates)
    {

      UpdatePacket packet = null;

      synchronized (updates)
      {
        this.updates.addAll(updates);
        if (this.updates.size() > 0)
        {
          packet = this.updates.remove(0);
        }
      }

      if (BGPv4FSM.this.managedChannels.size() != 1)
      {
        BGPv4FSM.this.internalFsm.flagFSMError();
      }
      else if (packet != null)
      {
        final Channel channel = BGPv4FSM.this.managedChannels.iterator().next().getChannel();
        channel.writeAndFlush(packet).addListener(this);
      }

    }

    @Override
    public void operationComplete(final ChannelFuture future) throws Exception
    {

      UpdatePacket packet = null;

      synchronized (this.updates)
      {
        if (this.updates.size() > 0)
        {
          packet = this.updates.remove(0);
        }
      }

      if (packet != null)
      {
        future.channel().write(packet).addListener(this);
      }
    }

  }

  private final BGPv4Client clientProvider;
  private final CapabilitesNegotiator capabilitiesNegotiator;
  private final PeerRoutingInformationBaseManager pribManager;
  private final OutboundRoutingUpdateQueue oruq;

  private final InternalFSM internalFsm;

  private PeerConfiguration peerConfig;
  private final ASType asTypeInUse = ASType.AS_NUMBER_2OCTETS;
  private final Set<FSMChannelImpl> managedChannels = new HashSet<FSMChannelImpl>();
  private PeerRoutingInformationBase prib;
  private final Set<AddressFamilyKey> outboundAddressFamilyMask = new HashSet<AddressFamilyKey>();

  public BGPv4FSM(final Scheduler scheduler, final BGPv4Client clientProvider, final CapabilitesNegotiator capabilitiesNegotiation,
      final PeerRoutingInformationBaseManager pribManager, final OutboundRoutingUpdateQueue orug)
  {
    this.clientProvider = clientProvider;
    this.capabilitiesNegotiator = capabilitiesNegotiation;
    this.pribManager = pribManager;
    this.oruq = orug;
    this.internalFsm = new InternalFSM(scheduler);
  }

  public void configure(final PeerConfiguration peerConfig) throws SchedulerException
  {
    this.peerConfig = peerConfig;
    this.internalFsm.setup(peerConfig, new InternalFSMCallbacksImpl());
    this.capabilitiesNegotiator.setup(peerConfig);
    this.oruq.setPeerName(peerConfig.getPeerName());
    this.oruq.setCallback(new SendLocalRoutingUpdateCallback());
  }

  public InetSocketAddress getRemotePeerAddress()
  {
    return this.peerConfig.getClientConfig().getRemoteAddress();
  }

  public PeerConnectionInformation getPeerConnectionInformation()
  {
    return new PeerConnectionInformationImpl();
  }

  public void startFSMAutomatic()
  {
    this.internalFsm.handleEvent(FSMEvent.automaticStart());
  }

  public void startFSMManual()
  {
    this.internalFsm.handleEvent(FSMEvent.manualStart());
  }

  public void stopFSM()
  {
    this.internalFsm.handleEvent(FSMEvent.automaticStop());
  }

  public void destroyFSM()
  {
    this.internalFsm.destroyFSM();
  }

  public void handleMessage(final Channel channel, final BGPv4Packet message)
  {

    if (message instanceof OpenPacket)
    {

      this.internalFsm.setPeerProposedHoldTime(((OpenPacket) message).getHoldTime());

      this.capabilitiesNegotiator.recordPeerCapabilities((OpenPacket) message);

      if (this.capabilitiesNegotiator.missingRequiredCapabilities().size() > 0)
      {
        for (final Capability cap : this.capabilitiesNegotiator.missingRequiredCapabilities())
        {
          BGPv4FSM.log.error("Missing required capability: " + cap);
        }

        this.internalFsm.handleEvent(FSMEvent.bgpOpenMessageError());
      }
      else
      {
        this.internalFsm.handleEvent(FSMEvent.bgpOpen(this.findWrapperForChannel(channel)));
      }
    }
    else if (message instanceof KeepalivePacket)
    {
      this.internalFsm.handleEvent(FSMEvent.keepAliveMessage());
    }
    else if (message instanceof UpdatePacket)
    {

      this.internalFsm.handleEvent(FSMEvent.updateMessage());

      try
      {
        this.processRemoteUpdate((UpdatePacket) message);
      }
      catch (final Exception e)
      {
        BGPv4FSM.log.error("error processing UPDATE packet from peer: {}", this.peerConfig.getPeerName(), e);
        this.internalFsm.handleEvent(FSMEvent.updateMessageError());
      }
    }
    else if (message instanceof UnsupportedVersionNumberNotificationPacket)
    {
      this.internalFsm.handleEvent(FSMEvent.notifyMessageVersionError());
    }
    else if (message instanceof OpenNotificationPacket)
    {
      this.internalFsm.handleEvent(FSMEvent.bgpOpenMessageError());
    }
    else if (message instanceof UpdateNotificationPacket)
    {
      this.internalFsm.handleEvent(FSMEvent.updateMessageError());
    }
    else if (message instanceof NotificationPacket)
    {
      this.internalFsm.handleEvent(FSMEvent.notifyMessage());
    }
  }

  public void handleEvent(final Channel channel, final BgpEvent message)
  {

    BGPv4FSM.log.info("received event: {}", message);

    if (message instanceof NotificationEvent)
    {
      for (final NotificationPacket packet : ((NotificationEvent) message).getNotifications())
      {
        if (packet instanceof UnsupportedVersionNumberNotificationPacket)
        {
          this.internalFsm.handleEvent(FSMEvent.notifyMessageVersionError());
        }
        else if (packet instanceof OpenNotificationPacket)
        {
          this.internalFsm.handleEvent(FSMEvent.bgpOpenMessageError());
        }
        else if (packet instanceof UpdateNotificationPacket)
        {
          this.internalFsm.handleEvent(FSMEvent.updateMessageError());
        }
        else
        {
          this.internalFsm.handleEvent(FSMEvent.notifyMessage());
        }
      }
    }
  }

  public void handleClientConnected(final Channel channel)
  {

    final FSMChannelImpl wrapper = this.findWrapperForChannel(channel);

    if (wrapper == null)
    {
      throw new RuntimeException("Expected to find channel");
    }

    this.internalFsm.handleEvent(FSMEvent.tcpConnectionRequestAcked(wrapper));

  }

  public void handleServerOpened(final Channel channel)
  {
    final FSMChannelImpl wrapper = new FSMChannelImpl(channel);

    this.managedChannels.add(wrapper);
    this.internalFsm.handleEvent(FSMEvent.tcpConnectionConfirmed(wrapper));
  }

  public void handleClosed(final Channel channel)
  {
    final FSMChannel wrapper = this.findWrapperForChannel(channel);

    if (wrapper != null)
    {
      this.internalFsm.handleEvent(FSMEvent.tcpConnectionFails(wrapper));
    }
  }

  public void handleDisconnected(final Channel channel)
  {
  }

  public boolean isCanAcceptConnection()
  {
    return this.internalFsm.isCanAcceptConnection();
  }

  public FSMState getState()
  {
    return this.internalFsm.getState();
  }

  private FSMChannelImpl findWrapperForChannel(final Channel channel)
  {
    FSMChannelImpl wrapper = null;

    for (final FSMChannelImpl impl : this.managedChannels)
    {
      if (impl.getChannel().equals(channel))
      {
        wrapper = impl;
        break;
      }
    }

    return wrapper;
  }

  /**
   * process the UPDATE packet received from the remote peer
   *
   * @param message
   */

  @SuppressWarnings("unchecked")
  private void processRemoteUpdate(final UpdatePacket message)
  {

    final Set<MultiProtocolReachableNLRI> mpReachables = message.lookupPathAttributes(MultiProtocolReachableNLRI.class);

    final Set<MultiProtocolUnreachableNLRI> mpUnreachables = message.lookupPathAttributes(MultiProtocolUnreachableNLRI.class);

    final Set<PathAttribute> otherAttributes = message.filterPathAttributes(
        MultiProtocolReachableNLRI.class,
        MultiProtocolUnreachableNLRI.class,
        NextHopPathAttribute.class);

    final AddressFamilyKey ipv4Unicast = new AddressFamilyKey(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING);

    if (mpReachables.size() > 0)
    {
      this.processRemoteUpdateMultiProtocolReachables(mpReachables, otherAttributes);
    }

    if (mpUnreachables.size() > 0)
    {
      this.processRemoteUp(mpUnreachables, otherAttributes);
    }

    if (!message.getWithdrawnRoutes().isEmpty())
    {
      // withdraw IPv4 prefixes
      this.prib.routingBase(RIBSide.Remote, ipv4Unicast).withdrawRoutes(message.getWithdrawnRoutes());
    }

    final Set<NextHopPathAttribute> nextHops = message.lookupPathAttributes(NextHopPathAttribute.class);

    if (nextHops.size() > 1)
    {
      throw new InvalidNextHopException();
    }

    this.prib.routingBase(RIBSide.Remote, ipv4Unicast).addRoutes(message.getNlris(), otherAttributes, (nextHops.isEmpty()) ? null : nextHops.iterator().next().getNextHop());

  }

  private void processRemoteUp(final Set<MultiProtocolUnreachableNLRI> mpUnreachables, final Set<PathAttribute> attrs)
  {
    
    
    for (final MultiProtocolUnreachableNLRI mp : mpUnreachables)
    {
      this.prib
          .routingBase(RIBSide.Remote, new AddressFamilyKey(mp.getAddressFamily(), mp.getSubsequentAddressFamily()))
          .withdrawRoutes(mp.getNlris());
    }
    
  }

  /**
   * 
   * @param mpReachables
   * @param attrs
   */

  private void processRemoteUpdateMultiProtocolReachables(final Set<MultiProtocolReachableNLRI> mpReachables, final Set<PathAttribute> attrs)
  {

    for (final MultiProtocolReachableNLRI mp : mpReachables)
    {
      this.prib
          .routingBase(RIBSide.Remote, new AddressFamilyKey(mp.getAddressFamily(), mp.getSubsequentAddressFamily()))
          .addRoutes(mp.getNlris(), attrs, mp.getNextHop());
    }

  }

}
