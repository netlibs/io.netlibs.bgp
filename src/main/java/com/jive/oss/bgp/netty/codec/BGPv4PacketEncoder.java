package com.jive.oss.bgp.netty.codec;

import com.jive.oss.bgp.netty.protocol.BGPv4PacketVisitor;
import com.jive.oss.bgp.netty.protocol.KeepalivePacket;
import com.jive.oss.bgp.netty.protocol.NotificationPacket;
import com.jive.oss.bgp.netty.protocol.open.OpenPacket;
import com.jive.oss.bgp.netty.protocol.refresh.RouteRefreshPacket;
import com.jive.oss.bgp.netty.protocol.update.UpdatePacket;

import io.netty.buffer.ByteBuf;

/**
 * Encodes any packets and returns a buffer.
 *
 * @author theo
 *
 */

public class BGPv4PacketEncoder implements BGPv4PacketVisitor<ByteBuf>
{

  @Override
  public ByteBuf visit(final OpenPacket pkt)
  {
    return pkt.encodePacket();
  }

  @Override
  public ByteBuf visit(final KeepalivePacket pkt)
  {
    return pkt.encodePacket();
  }

  @Override
  public ByteBuf visit(final NotificationPacket pkt)
  {
    return pkt.encodePacket();
  }

  @Override
  public ByteBuf visit(final UpdatePacket pkt)
  {
    return pkt.encodePacket();
  }

  @Override
  public ByteBuf visit(final RouteRefreshPacket pkt)
  {
    return pkt.encodePacket();
  }

}
