package io.joss.bgp.netty.protocol;

import io.joss.bgp.netty.protocol.open.OpenPacket;
import io.joss.bgp.netty.protocol.refresh.RouteRefreshPacket;
import io.joss.bgp.netty.protocol.update.UpdatePacket;

/**
 * Visit each of the base packet types.
 *
 * @author theo
 */

public interface BGPv4PacketVisitor<T>
{

  /**
   *
   */

  T visit(OpenPacket pkt);

  /**
   *
   */

  T visit(KeepalivePacket pkt);

  /**
   *
   */

  T visit(NotificationPacket pkt);

  /**
   *
   */

  T visit(UpdatePacket pkt);

  /**
   *
   */

  T visit(RouteRefreshPacket pkt);

}
