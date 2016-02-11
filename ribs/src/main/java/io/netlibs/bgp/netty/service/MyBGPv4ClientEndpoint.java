package io.netlibs.bgp.netty.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class MyBGPv4ClientEndpoint extends SimpleChannelInboundHandler<Object>
{

  @Override
  protected void channelRead0(final ChannelHandlerContext ctx, final Object msg) throws Exception
  {
    System.err.println(msg);
  }

}
