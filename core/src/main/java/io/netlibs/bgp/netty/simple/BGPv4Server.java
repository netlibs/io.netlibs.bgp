package io.netlibs.bgp.netty.simple;

import java.net.InetSocketAddress;

import io.netlibs.bgp.handlers.BGPv4Codec;
import io.netlibs.bgp.handlers.BGPv4Reframer;
import io.netlibs.bgp.netty.simple.handlers.RemoteOpenProcessor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Service which listens for incoming BGP connections.
 * 
 * Once connected, the provided factory is used to generating a session listener.
 * 
 * @author theo
 *
 */

public class BGPv4Server
{

  private final NioEventLoopGroup workers;
  private final BGPv4SessionFactory listener;
  private Channel channel;

  public BGPv4Server(NioEventLoopGroup workers, BGPv4SessionFactory listener)
  {
    this.workers = workers;
    this.listener = listener;
  }

  /**
   * Starts listening in the background.
   * 
   * @return
   */

  public InetSocketAddress start(InetSocketAddress listen)
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
        ch.pipeline().addLast(RemoteOpenProcessor.HANDLER_NAME, new RemoteOpenProcessor(listener));
      }

    });

    bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
    bootstrap.childOption(ChannelOption.TCP_NODELAY, true);

    this.channel = bootstrap.bind(listen).syncUninterruptibly().channel();

    return listenSocketAddress();

  }

  /**
   * Closes the server, which does not affect existing connections.
   */

  public void close()
  {
    channel.close().syncUninterruptibly();
  }

  /**
   * returns the socket address this server instance is listening on.
   * 
   * note that if it's 0.0.0.0/::0, then you'll need to convert that into one of the IPs on the machine.
   * 
   * @return
   */

  public InetSocketAddress listenSocketAddress()
  {
    return (InetSocketAddress) this.channel.localAddress();
  }

}
