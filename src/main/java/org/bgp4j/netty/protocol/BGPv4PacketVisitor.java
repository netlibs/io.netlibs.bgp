package org.bgp4j.netty.protocol;

import org.bgp4j.netty.protocol.open.OpenPacket;
import org.bgp4j.netty.protocol.refresh.RouteRefreshPacket;
import org.bgp4j.netty.protocol.update.UpdatePacket;

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
