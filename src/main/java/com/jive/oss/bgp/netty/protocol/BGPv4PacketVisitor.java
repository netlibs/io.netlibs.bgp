package com.jive.oss.bgp.netty.protocol;

import com.jive.oss.bgp.netty.protocol.open.OpenPacket;
import com.jive.oss.bgp.netty.protocol.refresh.RouteRefreshPacket;
import com.jive.oss.bgp.netty.protocol.update.UpdatePacket;

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
