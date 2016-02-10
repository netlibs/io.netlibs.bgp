package io.netlibs.bgp.netty.simple;

import com.jive.oss.commons.ip.IPv4Address;

import io.netlibs.bgp.netty.BGPv4Constants;
import io.netlibs.bgp.netty.PeerConnectionInformation;
import io.netlibs.bgp.netty.handlers.BgpEvent;
import io.netlibs.bgp.netty.protocol.BGPv4Packet;
import io.netlibs.bgp.netty.protocol.KeepalivePacket;
import io.netlibs.bgp.netty.protocol.open.OpenPacket;
import io.netlibs.bgp.netty.protocol.update.UpdatePacket;
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


      // send an OPEN packet once we see one.

      final OpenPacket packet = new OpenPacket();

      packet.setAutonomousSystem(open.getAutonomousSystem());
      packet.setBgpIdentifier(IPv4Address.fromString("104.197.97.174").longValue());
      packet.setHoldTime(30);
      packet.setProtocolVersion(BGPv4Constants.BGP_VERSION);

      // BGPv4FSM.this.capabilitiesNegotiator.insertLocalCapabilities(packet);

      ctx.channel().write(packet);
      ctx.channel().writeAndFlush(new KeepalivePacket());
      
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


  }


  @Override
  public void channelInactive(final ChannelHandlerContext ctx) throws Exception
  {
    log.info("disconnected from client {}", ctx.channel().remoteAddress());
    ctx.fireChannelInactive();
    listener.close();
  }

  @Override
  public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception
  {
    ctx.fireChannelUnregistered();
  }

}
