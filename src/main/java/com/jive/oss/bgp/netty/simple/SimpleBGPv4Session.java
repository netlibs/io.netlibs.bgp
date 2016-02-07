package com.jive.oss.bgp.netty.simple;

import com.jive.oss.bgp.netty.BGPv4Constants;
import com.jive.oss.bgp.netty.PeerConnectionInformation;
import com.jive.oss.bgp.netty.handlers.BgpEvent;
import com.jive.oss.bgp.netty.protocol.BGPv4Packet;
import com.jive.oss.bgp.netty.protocol.KeepalivePacket;
import com.jive.oss.bgp.netty.protocol.open.OpenPacket;
import com.jive.oss.bgp.netty.protocol.update.UpdatePacket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleBGPv4Session extends SimpleChannelInboundHandler<Object>
{

  public static final String HANDLER_NAME = "SimpleBGPv4Session";

  private final SimpleSessionListener listener;

  public SimpleBGPv4Session(SimpleSessionListener listener)
  {
    this.listener = listener;
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
          // hold time has expired.  fml.
          log.info("Hold time has expired, closing.");
          break;
        case READER_IDLE:
          break;
        case WRITER_IDLE:
          ctx.channel().writeAndFlush(new KeepalivePacket());
          break;
      }
    }
  }

  private void channelRead(ChannelHandlerContext ctx, BGPv4Packet e)
  {

    if (e instanceof UpdatePacket)
    {
      listener.update((UpdatePacket) e);
    }
    else if (e instanceof KeepalivePacket)
    {
      listener.keepalive((KeepalivePacket) e);
    }
    else if (e instanceof OpenPacket)
    {

      OpenPacket open = (OpenPacket) e;
      log.debug("Adding hold timer: {}", open.getHoldTime());

      listener.open((OpenPacket) e);

      ctx.channel().pipeline().addBefore(SimpleBGPv4Session.HANDLER_NAME, "keepalive", new IdleStateHandler(open.getHoldTime(), 5, 0));

    }
    else
    {
      log.warn("Unhandled BGP packet: {}", e);
    }

  }

  private void channelRead(ChannelHandlerContext ctx, BgpEvent e)
  {
    System.err.println(e);
  }

  @Override
  public void channelActive(final ChannelHandlerContext ctx) throws Exception
  {

    log.info("Connected to BGPv4 peer: {}", ctx.channel().remoteAddress());

    ctx.fireChannelActive();

    // send an OPEN packet.

    final OpenPacket packet = new OpenPacket();

    //
    packet.setAutonomousSystem(6643);
    packet.setBgpIdentifier(1);
    packet.setHoldTime(30);
    packet.setProtocolVersion(BGPv4Constants.BGP_VERSION);

    // BGPv4FSM.this.capabilitiesNegotiator.insertLocalCapabilities(packet);

    ctx.channel().write(packet);
    ctx.channel().writeAndFlush(new KeepalivePacket());

    // we now need to schedule a timer to send a keepalive.

  }

  public static final AttributeKey<PeerConnectionInformation> PEER_CONNECTION_INFO = AttributeKey.valueOf("peerConnection");

  @Override
  public void channelInactive(final ChannelHandlerContext ctx) throws Exception
  {
    log.info("disconnected from client " + ctx.channel().remoteAddress());
    ctx.fireChannelInactive();
    listener.close();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jboss.netty.channel.SimpleChannelHandler#channelClosed(org.jboss.netty.channel.ChannelHandlerContext,
   * org.jboss.netty.channel.ChannelStateEvent)
   */

  @Override
  public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception
  {
    log.info("channel was closed to client {}", ctx.channel().remoteAddress());
    ctx.fireChannelUnregistered();
  }

}
