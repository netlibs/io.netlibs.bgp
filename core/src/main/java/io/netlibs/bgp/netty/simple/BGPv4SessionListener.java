package io.netlibs.bgp.netty.simple;

import io.netlibs.bgp.handlers.NotificationEvent;
import io.netlibs.bgp.netty.protocol.KeepalivePacket;
import io.netlibs.bgp.netty.protocol.open.OpenPacket;
import io.netlibs.bgp.netty.protocol.update.UpdatePacket;

/**
 * @author theo
 */

public interface BGPv4SessionListener
{

  /**
   * the connection is opened. from here on in, messages must be read using session.read(callback).
   */

  void open(BGPv4Session session, OpenPacket remoteOpen);

  /**
   * received an update.
   */

  void update(UpdatePacket e);

  /**
   * a notification event was received.
   */

  void notification(NotificationEvent e);

  /**
   * The connection was closed.
   */

  void close();

}
