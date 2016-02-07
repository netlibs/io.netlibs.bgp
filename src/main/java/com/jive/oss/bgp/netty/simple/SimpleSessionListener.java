package com.jive.oss.bgp.netty.simple;

import com.jive.oss.bgp.netty.handlers.NotificationEvent;
import com.jive.oss.bgp.netty.protocol.KeepalivePacket;
import com.jive.oss.bgp.netty.protocol.open.OpenPacket;
import com.jive.oss.bgp.netty.protocol.update.UpdatePacket;

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
