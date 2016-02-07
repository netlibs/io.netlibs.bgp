package com.jive.oss.bgp.netty.simple;

import java.util.function.Supplier;

import com.jive.oss.bgp.netty.handlers.BGPv4Codec;
import com.jive.oss.bgp.netty.handlers.BGPv4Reframer;
import com.jive.oss.bgp.netty.handlers.InboundOpenCapabilitiesProcessor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Listen.
 * 
 * @author theo
 *
 */

public class SimpleBGPv4Server
{

  private Channel channel;
  private Supplier<SimpleSessionListener> listener;

  public SimpleBGPv4Server(Supplier<SimpleSessionListener> listener)
  {
    this.listener = listener;
  }

  /**
   * 
   */

  public void start(int port)
  {

    ServerBootstrap bootstrap = new ServerBootstrap();

    bootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup());

    bootstrap.channel(NioServerSocketChannel.class);
    bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
    bootstrap.option(ChannelOption.SO_REUSEADDR, true);

    bootstrap.option(ChannelOption.TCP_NODELAY, true);

    bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

      @Override
      public void initChannel(final SocketChannel ch) throws Exception
      {
        ch.pipeline().addLast(BGPv4Reframer.HANDLER_NAME, new BGPv4Reframer());
        ch.pipeline().addLast(BGPv4Codec.HANDLER_NAME, new BGPv4Codec());
        ch.pipeline().addLast(InboundOpenCapabilitiesProcessor.HANDLER_NAME, new InboundOpenCapabilitiesProcessor());
        // ch.pipeline().addLast(ValidateServerIdentifier.HANDLER_NAME, new ValidateServerIdentifier());
        ch.pipeline().addLast(SimpleBGPv4Session.HANDLER_NAME, new SimpleBGPv4Session(listener.get()));
      }

    });

    bootstrap.option(ChannelOption.SO_BACKLOG, 128);

    bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
    bootstrap.childOption(ChannelOption.TCP_NODELAY, true);

    this.channel = bootstrap.bind(port).syncUninterruptibly().channel();

  }

}
