package io.joss.bgp.netty.codec;

import io.joss.bgp.netty.BGPv4Constants;
import io.joss.bgp.netty.protocol.KeepalivePacket;
import io.netty.buffer.ByteBuf;

public class KeepalivePacketDecoder
{

  /**
   * decode the KEEPALIVE network packet. The OPEN packet must be exactly 0 octets large at this point.
   *
   * @param buffer
   *          the buffer containing the data.
   * @return
   */

  KeepalivePacket decodeKeepalivePacket(final ByteBuf buffer)
  {
    final KeepalivePacket packet = new KeepalivePacket();
    ProtocolPacketUtils.verifyPacketSize(buffer, BGPv4Constants.BGP_PACKET_SIZE_KEEPALIVE, BGPv4Constants.BGP_PACKET_SIZE_KEEPALIVE);
    return packet;
  }

}
