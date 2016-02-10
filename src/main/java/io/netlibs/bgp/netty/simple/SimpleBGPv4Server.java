package io.netlibs.bgp.netty.simple;

import io.netlibs.bgp.netty.handlers.BGPv4Codec;
import io.netlibs.bgp.netty.handlers.BGPv4Reframer;
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

  private final NioEventLoopGroup workers = new NioEventLoopGroup(1);

  private Channel channel;
  private SimpleSessionProvider listener;

  public SimpleBGPv4Server(SimpleSessionProvider listener)
  {
    this.listener = listener;
  }

  /**
   * 
   */

  public void start(int port)
  {

    ServerBootstrap bootstrap = new ServerBootstrap();

    bootstrap.group(workers);

    bootstrap.channel(NioServerSocketChannel.class);

    bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
    bootstrap.option(ChannelOption.SO_REUSEADDR, true);

    bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

      @Override
      public void initChannel(final SocketChannel ch) throws Exception
      {
        ch.pipeline().addLast(BGPv4Reframer.HANDLER_NAME, new BGPv4Reframer());
        ch.pipeline().addLast(BGPv4Codec.HANDLER_NAME, new BGPv4Codec());
        ch.pipeline().addLast(SimpleRemoteOpenProcessor.HANDLER_NAME, new SimpleRemoteOpenProcessor(listener));
      }

    });

    bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
    bootstrap.childOption(ChannelOption.TCP_NODELAY, true);

    this.channel = bootstrap.bind(port).syncUninterruptibly().channel();

  }

}
