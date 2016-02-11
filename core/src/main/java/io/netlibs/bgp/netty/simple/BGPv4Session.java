package io.netlibs.bgp.netty.simple;

import java.net.InetSocketAddress;

import io.netlibs.bgp.netty.protocol.open.OpenPacket;

public interface BGPv4Session
{

  OpenPacket openPacket();

  InetSocketAddress remoteAddress();

  /**
   * The input buffer.
   */

  ReadTransferBuffer input();
  
}
