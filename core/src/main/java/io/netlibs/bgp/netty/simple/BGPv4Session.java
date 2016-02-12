package io.netlibs.bgp.netty.simple;

import java.net.InetSocketAddress;

import io.netlibs.bgp.netty.protocol.open.OpenPacket;
import io.netlibs.bgp.netty.protocol.update.UpdatePacket;

public interface BGPv4Session
{

  /**
   * Sends one or more UPDATEs.
   * 
   * 
   * 
   * @param done
   *          The callback when the last packet has been sent.
   * 
   * @param updates
   *          The {@link UpdatePacket} instances to send.
   * 
   */

  void send(Runnable done, UpdatePacket... updates);

  /**
   * The OPEN received from the remote side.
   */

  OpenPacket openPacket();

  /**
   * Peer address.
   */
  
  InetSocketAddress remoteAddress();

  /**
   * The input buffer.
   */

  ReadTransferBuffer input();

}
