package io.joss.bgp.netty.simple;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import com.google.common.net.HostAndPort;

import io.joss.bgp.netty.codec.BGPv4PacketDecoder;
import io.joss.bgp.netty.handlers.BGPv4ClientEndpoint;
import io.joss.bgp.netty.handlers.BGPv4Codec;
import io.joss.bgp.netty.handlers.BGPv4Reframer;
import io.joss.bgp.netty.handlers.InboundOpenCapabilitiesProcessor;
import io.joss.bgp.netty.handlers.ValidateServerIdentifier;
import io.joss.bgp.netty.service.BGPv4Client;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple netty BGPv4 client which keeps an active BGP connection open, and services it; currently only supports receiving routes, not
 * sending any.
 * 
 * @author theo
 *
 */

@Slf4j
public class SimpleNettyClient
{

  private static final EventLoopGroup workerGroup = new NioEventLoopGroup(1);

  /**
   * The target peer.
   */

  private final HostAndPort target;
  private SimpleSessionListener listener;

  public SimpleNettyClient(HostAndPort target)
  {
    this.target = target;
  }

  public void setListener(SimpleSessionListener listener)
  {
    this.listener = listener;
  }

  /**
   * Starts connection using the BGPv4.
   */

  public void start()
  {

    connect();

  }

  private void connect()
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
        ch.pipeline().addLast(InboundOpenCapabilitiesProcessor.HANDLER_NAME, new InboundOpenCapabilitiesProcessor());
        // ch.pipeline().addLast(ValidateServerIdentifier.HANDLER_NAME, new ValidateServerIdentifier());
        ch.pipeline().addLast(SimpleBGPv4Session.HANDLER_NAME, new SimpleBGPv4Session(listener));
      }

    });

    // Start the client.
    ChannelFuture future = b.connect(new InetSocketAddress(target.getHostText(), target.getPortOrDefault(179)));

    future.addListener(fut -> {

      if (!fut.isSuccess())
      {
        reschedule();
      }
      else
      {
        future.channel().closeFuture().addListener(closefuture -> closed());
      }

    });

  }

  void reschedule()
  {
    log.debug("Rescheduling");
    workerGroup.schedule(this::connect, 15, TimeUnit.SECONDS);
  }

  void closed()
  {
    log.debug("Connection closed after being opened");
    reschedule();
  }

}
