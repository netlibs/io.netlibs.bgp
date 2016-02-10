package io.netlibs.bgp.netty.codec;

import io.netlibs.bgp.netty.protocol.BGPv4PacketVisitor;
import io.netlibs.bgp.netty.protocol.KeepalivePacket;
import io.netlibs.bgp.netty.protocol.NotificationPacket;
import io.netlibs.bgp.netty.protocol.open.OpenPacket;
import io.netlibs.bgp.netty.protocol.refresh.RouteRefreshPacket;
import io.netlibs.bgp.netty.protocol.update.UpdatePacket;
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
