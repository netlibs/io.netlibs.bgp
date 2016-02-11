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
 * File: org.bgp4j.netty.fsm.FSMChannelManager.java
 */
package io.netlibs.bgp.netty.fsm;

import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@Slf4j
class InternalFSMChannelManager
{

  private FSMChannel managedChannel;
  private final InternalFSMCallbacks callbacks;
  private final List<FSMEventType> inboundEventStream = new LinkedList<FSMEventType>();
  private final List<FSMEventType> outboundEventStream = new LinkedList<FSMEventType>();

  InternalFSMChannelManager(final InternalFSMCallbacks callbacks)
  {
    this.callbacks = callbacks;
  }

  void connect(final FSMChannel managedChannel)
  {
    this.disconnect();
    this.managedChannel = managedChannel;
  }

  void disconnect()
  {
    if (this.isConnected())
    {
      this.fireDisconnectRemotePeer();
    }
    this.clear();
  }

  /**
   * The remote connection to the peer (if established) shall be disconnected and closed
   */
  void fireDisconnectRemotePeer()
  {
    if (this.managedChannel != null)
    {
      this.callbacks.fireDisconnectRemotePeer(this.managedChannel);
    }
  }

  /**
   * Sent an <code>OPEN</code> message to the remote peer.
   */
  void fireSendOpenMessage()
  {
    if (this.managedChannel != null)
    {
      this.callbacks.fireSendOpenMessage(this.managedChannel);
      this.outboundEventStream.add(FSMEventType.BGPOpen);
    }
  }

  /**
   * send an FSM error notification to the remote peer
   */
  void fireSendInternalErrorNotification()
  {
    if (this.managedChannel != null)
    {
      this.callbacks.fireSendInternalErrorNotification(this.managedChannel);
      this.outboundEventStream.add(FSMEventType.NotifyMsg);
    }
  }

  /**
   * send a CEASE notification to the remote peer
   */
  void fireSendCeaseNotification()
  {
    if (this.managedChannel != null)
    {
      this.callbacks.fireSendCeaseNotification(this.managedChannel);
      this.outboundEventStream.add(FSMEventType.NotifyMsg);
    }
  }

  /**
   * send a keepalive message to the remote peer
   */
  void fireSendKeepaliveMessage()
  {
    if (this.managedChannel != null)
    {
      this.callbacks.fireSendKeepaliveMessage(this.managedChannel);
      this.outboundEventStream.add(FSMEventType.KeepAliveMsg);
    }
  }

  /**
   * fire a notification to the peer that the hold timer expired
   */
  void fireSendHoldTimerExpiredNotification()
  {
    if (this.managedChannel != null)
    {
      this.callbacks.fireSendHoldTimerExpiredNotification(this.managedChannel);
      this.outboundEventStream.add(FSMEventType.NotifyMsg);
    }
  }

  /**
   * fire an notification to the peer that it sent a bad update
   */
  void fireSendUpdateErrorNotification()
  {
    if (this.managedChannel != null)
    {
      this.callbacks.fireSendUpdateErrorNotification(this.managedChannel);
      this.outboundEventStream.add(FSMEventType.NotifyMsg);
    }
  }

  boolean isConnected()
  {
    return (this.managedChannel != null);
  }

  boolean isManagedChannel(final FSMChannel channel)
  {
    return (this.managedChannel == channel);
  }

  void clear()
  {
    this.managedChannel = null;
    this.inboundEventStream.clear();
    this.outboundEventStream.clear();
  }

  boolean hasSeenInboundFSMEvent(final FSMEventType event)
  {
    return this.inboundEventStream.contains(event);
  }

  boolean hasSeenOutbboundFSMEvent(final FSMEventType event)
  {
    return this.outboundEventStream.contains(event);
  }

  void pushInboundFSMEvent(final FSMEventType event)
  {
    if (this.isConnected())
    {
      this.inboundEventStream.add(event);
    }
  }
}
