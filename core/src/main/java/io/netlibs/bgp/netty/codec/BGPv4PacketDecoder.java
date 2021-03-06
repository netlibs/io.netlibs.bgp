
package io.netlibs.bgp.netty.codec;

import io.netlibs.bgp.netty.protocol.BGPv4Packet;
import io.netlibs.bgp.netty.protocol.BadMessageTypeException;
import io.netlibs.bgp.protocol.BGPv4Constants;
import io.netty.buffer.ByteBuf;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

public class BGPv4PacketDecoder
{

  static final OpenPacketDecoder openPacketDecoder = new OpenPacketDecoder();
  static final UpdatePacketDecoder updatePacketDecoder = new UpdatePacketDecoder();
  static final NotificationPacketDecoder notificationPacketDecoder = new NotificationPacketDecoder();
  static final RouteRefreshPacketDecoder routeRefreshPacketDecoder = new RouteRefreshPacketDecoder();
  static final KeepalivePacketDecoder keepalivePacketDecoder = new KeepalivePacketDecoder();

  private static final BGPv4PacketDecoder INSTANCE = new BGPv4PacketDecoder();

  public BGPv4Packet decodePacket(final ByteBuf buffer)
  {

    final int type = buffer.readUnsignedByte();

    BGPv4Packet packet = null;

    switch (type)
    {

      case BGPv4Constants.BGP_PACKET_TYPE_OPEN:
        packet = openPacketDecoder.decodeOpenPacket(buffer);
        break;

      case BGPv4Constants.BGP_PACKET_TYPE_UPDATE:
        packet = updatePacketDecoder.decodeUpdatePacket(buffer);
        break;

      case BGPv4Constants.BGP_PACKET_TYPE_NOTIFICATION:
        packet = notificationPacketDecoder.decodeNotificationPacket(buffer);
        break;

      case BGPv4Constants.BGP_PACKET_TYPE_KEEPALIVE:
        packet = keepalivePacketDecoder.decodeKeepalivePacket(buffer);
        break;

      case BGPv4Constants.BGP_PACKET_TYPE_ROUTE_REFRESH:
        packet = routeRefreshPacketDecoder.decodeRouteRefreshPacket(buffer);
        break;

      default:
        // this seems to most commonly indicate a framing problem due to previously invalid message.
        throw new BadMessageTypeException(type);

    }

    return packet;
  }

  public static BGPv4PacketDecoder getInstance()
  {
    return INSTANCE;
  }

}
