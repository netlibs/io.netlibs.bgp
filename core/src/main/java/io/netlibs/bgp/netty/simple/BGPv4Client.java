package io.netlibs.bgp.netty.simple;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import com.google.common.net.HostAndPort;

import io.netlibs.bgp.handlers.BGPv4Codec;
import io.netlibs.bgp.handlers.BGPv4Reframer;
import io.netlibs.bgp.handlers.InboundOpenCapabilitiesProcessor;
import io.netlibs.bgp.netty.simple.handlers.LocalOpenProcessor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple netty BGPv4 client which keeps an active BGP connection open, and services it; currently only supports receiving routes, not
 * sending any.
 * 
 * @author theo
 *
 */

@Slf4j
public class BGPv4Client
{

  /**
   * The target peer.
   */

  private final HostAndPort target;
  private BGPv4SessionListener listener;
  private final EventLoopGroup workerGroup;
  private LocalConfig config;

  public BGPv4Client(EventLoopGroup workerGroup, HostAndPort target, LocalConfig config)
  {
    this.target = target;
    this.workerGroup = workerGroup;
    this.config = config;
  }

  public void setListener(BGPv4SessionListener listener)
  {
    this.listener = listener;
  }

  public void connect()
  {

    final Bootstrap b = new Bootstrap();

    b.group(workerGroup);
    b.channel(NioSocketChannel.class);

    b.option(ChannelOption.TCP_NODELAY, true);
    b.option(ChannelOption.SO_KEEPALIVE, true);

    b.handler(new ChannelInitializer<SocketChannel>() {

      @Override
      public void initChannel(final SocketChannel ch) throws Exception
      {
        ch.pipeline().addLast(BGPv4Reframer.HANDLER_NAME, new BGPv4Reframer());
        ch.pipeline().addLast(BGPv4Codec.HANDLER_NAME, new BGPv4Codec());
        // ch.pipeline().addLast(InboundOpenCapabilitiesProcessor.HANDLER_NAME, new InboundOpenCapabilitiesProcessor());
        // ch.pipeline().addLast(ValidateServerIdentifier.HANDLER_NAME, new ValidateServerIdentifier());

        ch.pipeline().addLast(LocalOpenProcessor.HANDLER_NAME, new LocalOpenProcessor(config, listener));

      }

    });

    // Start the client.
    ChannelFuture future = b.connect(new InetSocketAddress(target.getHostText(), target.getPortOrDefault(179)));

    future.addListener(fut -> {

      if (!fut.isSuccess())
      {
        failed();
      }
      else
      {
        future.channel().closeFuture().addListener(closefuture -> closed());
      }

    });

  }

  void failed()
  {
    log.info("Connection failed");
    listener.close();
  }

  void closed()
  {
    log.info("Connection closed after being opened");
    listener.close();
  }

}
