package io.netlibs.bgp.net;

import io.netlibs.bgp.handlers.NotificationEvent;
import io.netlibs.bgp.netty.protocol.KeepalivePacket;
import io.netlibs.bgp.netty.protocol.open.OpenPacket;
import io.netlibs.bgp.netty.protocol.update.UpdatePacket;
import io.netlibs.bgp.netty.simple.BGPv4Session;
import io.netlibs.bgp.netty.simple.BGPv4SessionListener;
import lombok.extern.slf4j.Slf4j;

/**
 * Listener for events from the peer. Push each event into the routing table, but perform a little buffering.
 */

@Slf4j
public class TestSessionHandler implements BGPv4SessionListener
{

  private BGPv4Session session;

  @Override
  public void open(BGPv4Session session, OpenPacket e)
  {
    log.info("Session OPENed with {}: {}", session.remoteAddress(), e);
    this.session = session;
  }

  @Override
  public void update(UpdatePacket e)
  {
    session.input().consume(1);
  }

  @Override
  public void notification(NotificationEvent e)
  {
    session.input().consume(1);
  }

  @Override
  public void close()
  {
  }

  @Override
  public void keepalive(KeepalivePacket e)
  {
    // TODO Auto-generated method stub
    
  }

}
