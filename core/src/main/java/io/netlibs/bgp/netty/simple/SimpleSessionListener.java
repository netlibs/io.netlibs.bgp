package io.netlibs.bgp.netty.simple;

import io.netlibs.bgp.handlers.NotificationEvent;
import io.netlibs.bgp.netty.protocol.KeepalivePacket;
import io.netlibs.bgp.netty.protocol.open.OpenPacket;
import io.netlibs.bgp.netty.protocol.update.UpdatePacket;

public interface SimpleSessionListener
{
  
  /**
   * the connection is opened.
   */
  
  void open(OpenPacket e);
  
  /**
   * received an update.
   */
  
  void update(UpdatePacket e);
  
  /**
   * the keepalive was received.
   */
  
  void keepalive(KeepalivePacket e);
  
  /**
   * 
   */
  
  void notification(NotificationEvent e);
  
  /**
   * The connection was closed.
   */
  
  void close();

}
