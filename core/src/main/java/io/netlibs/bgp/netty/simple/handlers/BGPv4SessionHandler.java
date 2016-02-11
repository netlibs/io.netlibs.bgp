package io.netlibs.bgp.netty.simple.handlers;

import java.net.InetSocketAddress;

import com.google.common.base.Preconditions;

import io.netlibs.bgp.handlers.BgpEvent;
import io.netlibs.bgp.netty.protocol.BGPv4Packet;
import io.netlibs.bgp.netty.protocol.KeepalivePacket;
import io.netlibs.bgp.netty.protocol.open.OpenPacket;
import io.netlibs.bgp.netty.protocol.update.UpdatePacket;
import io.netlibs.bgp.netty.simple.BGPv4Session;
import io.netlibs.bgp.netty.simple.BGPv4SessionListener;
import io.netlibs.bgp.netty.simple.ReadTransferBuffer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty handler which processing incoming BGP messages, ensuring flow control.
 * 
 * @author theo
 *
 */

@Slf4j
public class BGPv4SessionHandler extends SimpleChannelInboundHandler<Object> implements BGPv4Session
{

  public static final String HANDLER_NAME = "SimpleBGPv4Session";

  private ReadTransferBuffer buffer = new ReadTransferBuffer();

  private final BGPv4SessionListener listener;

  private ChannelHandlerContext ctx;

  private OpenPacket openPacket;

  public BGPv4SessionHandler(BGPv4SessionListener listener, OpenPacket open)
  {
    this.listener = Preconditions.checkNotNull(listener);
    this.openPacket = open;
  }

  @Override
  protected void channelRead0(final ChannelHandlerContext ctx, final Object e) throws Exception
  {

    if (e instanceof BGPv4Packet)
    {
      channelRead(ctx, (BGPv4Packet) e);
    }
    else if (e instanceof BgpEvent)
    {
      channelRead(ctx, (BgpEvent) e);
    }
    else
    {
      log.error("unknown payload class " + e.getClass().getName() + " received for peer " + ctx.channel().remoteAddress());
    }

  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object e) throws Exception
  {
    if (e instanceof IdleStateEvent)
    {
      IdleStateEvent idle = (IdleStateEvent) e;
      switch (idle.state())
      {
        case ALL_IDLE:
          // hold time has expired. fml.
          log.info("Hold time has expired, closing.");
          break;
        case READER_IDLE:
          break;
        case WRITER_IDLE:
          ctx.channel().writeAndFlush(new KeepalivePacket());
          break;
      }
    }
    else
    {
      super.userEventTriggered(ctx, e);
    }
  }

  private void channelRead(ChannelHandlerContext ctx, BGPv4Packet e)
  {
    // data has arrived, but we don't push - instead send to the ReadTransferBuffer.    
    buffer.enqueue(e);
  }

  private void channelRead(ChannelHandlerContext ctx, BgpEvent e)
  {
    System.err.println(e);
  }

  void initialize(ChannelHandlerContext ctx)
  {
    if (this.ctx != null)
    {
      return;
    }
    this.ctx = ctx;
    this.listener.open(this, openPacket());
    ctx.channel().config().setAutoRead(false);
    buffer.init(() -> ctx.read(), listener);
    buffer.channelReadable();
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
  {
    buffer.channelReadable();
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception
  {
    if (ctx.channel().isActive() && ctx.channel().isRegistered())
    {
      // channelActvie() event has been fired already, which means this.channelActive() will
      // not be invoked. We have to initialize here instead.
      initialize(ctx);
    }
    else
    {
      // channelActive() event has not been fired yet. this.channelActive() will be invoked
      // and initialization will occur there.
    }
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
  {
    // nothign to do ...
  }

  @Override
  public void channelActive(final ChannelHandlerContext ctx) throws Exception
  {
    log.debug("BGPv4 Session Handler: ACTIVE");
    initialize(ctx);
    ctx.fireChannelActive();
  }

  @Override
  public void channelInactive(final ChannelHandlerContext ctx) throws Exception
  {
    log.debug("BGPv4 Session Handler: INACTIVE");
    log.info("disconnected from client {}", ctx.channel().remoteAddress());
    ctx.fireChannelInactive();
    listener.close();
  }

  @Override
  public void channelRegistered(final ChannelHandlerContext ctx) throws Exception
  {
    log.debug("BGPv4 Session Handler: REGISTERED");
    if (ctx.channel().isActive())
    {
      initialize(ctx);
    }
    ctx.fireChannelRegistered();
  }

  @Override
  public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception
  {
    log.debug("BGPv4 Session Handler: UNREGISTERED");
    ctx.fireChannelUnregistered();
  }

  @Override
  public InetSocketAddress remoteAddress()
  {
    return (InetSocketAddress) this.ctx.channel().remoteAddress();
  }

  @Override
  public OpenPacket openPacket()
  {
    return this.openPacket;
  }

  @Override
  public ReadTransferBuffer input()
  {
    return this.buffer;
  }

}
