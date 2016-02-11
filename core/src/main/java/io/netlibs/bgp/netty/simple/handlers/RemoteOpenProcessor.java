
package io.netlibs.bgp.netty.simple.handlers;

import java.net.InetSocketAddress;

import io.netlibs.bgp.netty.protocol.KeepalivePacket;
import io.netlibs.bgp.netty.protocol.open.OpenPacket;
import io.netlibs.bgp.netty.simple.BGPv4SessionHandler;
import io.netlibs.bgp.netty.simple.BGPv4SessionFactory;
import io.netlibs.bgp.netty.simple.BGPv4SessionListener;
import io.netlibs.bgp.netty.simple.LocalConfig;
import io.netlibs.bgp.netty.simple.RemoteConfig;
import io.netlibs.bgp.protocol.BGPv4Constants;
import io.netlibs.ipaddr.IPv4Address;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Sits in the pipeline to handle the OPEN. Once it's received, it sends one itself and adds the BGP handler.
 * 
 * @author theo
 *
 */

@Slf4j
public class RemoteOpenProcessor extends SimpleChannelInboundHandler<OpenPacket>
{

  public static final String HANDLER_NAME = "BGPv4-RemoteOpenProcessor";
  private BGPv4SessionFactory listener;

  public RemoteOpenProcessor(BGPv4SessionFactory listener)
  {
    this.listener = listener;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, OpenPacket open) throws Exception
  {

    IPv4Address addr = IPv4Address.fromString(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress());

    log.debug("Received OPEN: {}", open);

    RemoteConfig rconfig = this.listener.allocate(addr, open);

    if (rconfig == null || rconfig.getListener() == null || rconfig.getLocalConfig() == null)
    {
      log.info("closing connection to {} due to null session handler", ctx.channel().remoteAddress());
      ctx.channel().close();
      return;
    }

    LocalConfig config = rconfig.getLocalConfig();

    // send an OPEN packet once we see one.

    final OpenPacket packet = new OpenPacket();

    packet.setAutonomousSystem(config.getAutonomousSystem());
    packet.setBgpIdentifier(config.getBgpIdentifier());
    packet.setHoldTime(config.getHoldTimeSeconds());
    packet.setProtocolVersion(BGPv4Constants.BGP_VERSION);

    // BGPv4FSM.this.capabilitiesNegotiator.insertLocalCapabilities(packet);

    ctx.write(packet);
    ctx.writeAndFlush(new KeepalivePacket());

    BGPv4SessionHandler handler = new BGPv4SessionHandler(rconfig.getListener(), open);
    
    ctx.channel().pipeline().replace(this, BGPv4SessionHandler.HANDLER_NAME, handler);

    ctx.channel().pipeline().addBefore(BGPv4SessionHandler.HANDLER_NAME, "keepalive", new IdleStateHandler(open.getHoldTime(), config.getHoldTimeSeconds() / 2, 0));

    
  }

}
