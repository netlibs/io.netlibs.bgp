
package io.netlibs.bgp.netty.simple.handlers;

import io.netlibs.bgp.netty.protocol.KeepalivePacket;
import io.netlibs.bgp.netty.protocol.open.OpenPacket;
import io.netlibs.bgp.netty.simple.BGPv4SessionListener;
import io.netlibs.bgp.netty.simple.LocalConfig;
import io.netlibs.bgp.protocol.BGPv4Constants;
import io.netlibs.ipaddr.IPv4Address;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends an OPEN once the connection is open.
 * 
 * @author theo
 *
 */

@Slf4j
public class LocalOpenProcessor extends SimpleChannelInboundHandler<OpenPacket>
{

  public static final String HANDLER_NAME = "BGPv4-LocalOpenProcessor";
  private BGPv4SessionListener listener;
  private LocalConfig config;

  public LocalOpenProcessor(LocalConfig config, BGPv4SessionListener listener)
  {
    this.config = config;
    this.listener = listener;
  }

  /**
   * initially wait for the {@link OpenPacket}.
   */

  protected void channelRead0(ChannelHandlerContext ctx, OpenPacket open) throws Exception
  {
    log.info("Got OPEN from remote side: {}", open);    
    ctx.pipeline().replace(this, BGPv4SessionHandler.HANDLER_NAME, new BGPv4SessionHandler(listener, open));
    ctx.channel().pipeline().addBefore(BGPv4SessionHandler.HANDLER_NAME, "keepalive", new IdleStateHandler(open.getHoldTime(), config.getHoldTimeSeconds() / 2, 0));
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception
  {

    log.info("TCP connection opened to {}", ctx.channel().remoteAddress());

    // IPv4Address addr = IPv4Address.fromString(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress());

    // send an OPEN packet once we see one.

    final OpenPacket packet = new OpenPacket();

    packet.setAutonomousSystem(config.getAutonomousSystem());
    packet.setBgpIdentifier(config.getBgpIdentifier());
    packet.setHoldTime(config.getHoldTimeSeconds());
    packet.setProtocolVersion(BGPv4Constants.BGP_VERSION);

    // BGPv4FSM.this.capabilitiesNegotiator.insertLocalCapabilities(packet);

    ctx.channel().write(packet);
    ctx.channel().writeAndFlush(new KeepalivePacket());
    
    ctx.fireChannelActive();


  }

}
