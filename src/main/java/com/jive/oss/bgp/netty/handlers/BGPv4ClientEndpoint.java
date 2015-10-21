/**
 *  Copyright 2012 Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.jive.oss.bgp.netty.handlers;

import java.net.InetSocketAddress;

import com.jive.oss.bgp.netty.PeerConnectionInformation;
import com.jive.oss.bgp.netty.PeerConnectionInformationAware;
import com.jive.oss.bgp.netty.fsm.BGPv4FSM;
import com.jive.oss.bgp.netty.fsm.FSMRegistry;
import com.jive.oss.bgp.netty.protocol.BGPv4Packet;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This handler acts as the client side pipeline end. It attaches the peer connection info to the channel context of all insterested
 * handlers when the channel is connected. Each message it receives is forwarded to the appropiate finite state machine instance.
 *
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@Slf4j
@RequiredArgsConstructor
public class BGPv4ClientEndpoint extends SimpleChannelInboundHandler<Object>
{

  public static final String HANDLER_NAME = "BGP4-ClientEndpoint";

  private final FSMRegistry fsmRegistry;

  /*
   * (non-Javadoc)
   *
   * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext,
   * org.jboss.netty.channel.MessageEvent)
   */

  @Override
  protected void channelRead0(final ChannelHandlerContext ctx, final Object e) throws Exception
  {

    final BGPv4FSM fsm = this.fsmRegistry.lookupFSM((InetSocketAddress) ctx.channel().remoteAddress());

    if (fsm == null)
    {
      log.error("Internal Error: client for address " + ctx.channel().remoteAddress() + " is unknown");
      ctx.channel().close();
    }
    else
    {
      if (e instanceof BGPv4Packet)
      {
        fsm.handleMessage(ctx.channel(), (BGPv4Packet) e);
      }
      else if (e instanceof BgpEvent)
      {
        fsm.handleEvent(ctx.channel(), (BgpEvent) e);
      }
      else
      {
        log.error("unknown payload class " + e.getClass().getName() + " received for peer " + ctx.channel().remoteAddress());
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext,
   * org.jboss.netty.channel.ChannelStateEvent)
   */
  @Override
  // public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception
  public void channelActive(final ChannelHandlerContext ctx) throws Exception
  {
    
    log.info("connected to client " + ctx.channel().remoteAddress());

    final BGPv4FSM fsm = this.fsmRegistry.lookupFSM((InetSocketAddress) ctx.channel().remoteAddress());

    if (fsm == null)
    {
      log.error("Internal Error: client for address " + ctx.channel().remoteAddress() + " is unknown");
      ctx.channel().close();
    }
    else
    {
      
      final ChannelPipeline pipeline = ctx.pipeline();
      final PeerConnectionInformation pci = fsm.getPeerConnectionInformation();

      pipeline.forEach(e -> {

        ChannelHandler handler = e.getValue();

        if (handler.getClass().isAnnotationPresent(PeerConnectionInformationAware.class))
        {
          log.info("attaching peer connection information " + pci + " to handler " + e.getKey() + " for client " + ctx.channel().remoteAddress());
          pipeline.context(e.getKey()).attr(PEER_CONNECTION_INFO).set(pci);
        }

      });

      fsm.handleClientConnected(ctx.channel());
      
      ctx.fireChannelActive();

    }

  }

  public static final AttributeKey<PeerConnectionInformation> PEER_CONNECTION_INFO = AttributeKey.valueOf("peerConnection");

  /*
   * (non-Javadoc)
   *
   * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext,
   * org.jboss.netty.channel.ChannelStateEvent)
   */
  @Override
  public void channelInactive(final ChannelHandlerContext ctx) throws Exception
  {

    log.info("disconnected from client " + ctx.channel().remoteAddress());

    final BGPv4FSM fsm = this.fsmRegistry.lookupFSM((InetSocketAddress) ctx.channel().remoteAddress());

    if (fsm == null)
    {
      log.error("Internal Error: client for address " + ctx.channel().remoteAddress() + " is unknown");

      ctx.channel().close();
    }
    else
    {

      fsm.handleDisconnected(ctx.channel());
      ctx.fireChannelInactive();

    }

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

    log.info("closed channel to client {}", ctx.channel().remoteAddress());

    final BGPv4FSM fsm = this.fsmRegistry.lookupFSM((InetSocketAddress) ctx.channel().remoteAddress());

    if (fsm == null)
    {
      log.error("Internal Error: client for address " + ctx.channel().remoteAddress() + " is unknown");
    }
    else
    {
      fsm.handleClosed(ctx.channel());
      ctx.fireChannelUnregistered();
    }

  }

}
