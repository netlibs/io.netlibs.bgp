package org.bgp4j.netty.codec;

import org.bgp4j.netty.protocol.BGPv4PacketVisitor;
import org.bgp4j.netty.protocol.KeepalivePacket;
import org.bgp4j.netty.protocol.NotificationPacket;
import org.bgp4j.netty.protocol.open.OpenPacket;
import org.bgp4j.netty.protocol.refresh.RouteRefreshPacket;
import org.bgp4j.netty.protocol.update.UpdatePacket;

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
