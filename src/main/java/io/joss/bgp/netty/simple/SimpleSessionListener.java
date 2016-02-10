package io.joss.bgp.netty.simple;

import io.joss.bgp.netty.handlers.NotificationEvent;
import io.joss.bgp.netty.protocol.KeepalivePacket;
import io.joss.bgp.netty.protocol.open.OpenPacket;
import io.joss.bgp.netty.protocol.update.UpdatePacket;

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
