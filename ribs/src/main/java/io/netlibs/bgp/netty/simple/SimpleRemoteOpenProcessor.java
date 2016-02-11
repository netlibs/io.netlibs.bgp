
package io.netlibs.bgp.netty.simple;

import java.net.InetSocketAddress;

import io.netlibs.bgp.netty.protocol.open.OpenPacket;
import io.netlibs.ipaddr.IPv4Address;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Sits in the pipeline to handle the OPEN. Once it's received, it sends one itself and adds the BGP handler.
 * 
 * @author theo
 *
 */

@Slf4j
public class SimpleRemoteOpenProcessor extends SimpleChannelInboundHandler<OpenPacket>
{

  public static final String HANDLER_NAME = "SimpleBGP4-RemoteOpenProcessor";
  private SimpleSessionProvider listener;

  public SimpleRemoteOpenProcessor(SimpleSessionProvider listener)
  {
    this.listener = listener;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, OpenPacket e) throws Exception
  {

    IPv4Address addr = IPv4Address.fromString(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress());

    ctx.pipeline().replace(this, SimpleBGPv4Session.HANDLER_NAME, new SimpleBGPv4Session(listener.allocate(addr, e)));

    ctx.fireChannelRead(e);

  }

}
