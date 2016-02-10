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
package io.joss.bgp.netty.handlers;

import java.net.InetSocketAddress;

import io.joss.bgp.netty.PeerConnectionInformation;
import io.joss.bgp.netty.PeerConnectionInformationAware;
import io.joss.bgp.netty.fsm.BGPv4FSM;
import io.joss.bgp.netty.fsm.FSMRegistry;
import io.joss.bgp.netty.protocol.BGPv4Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This handler acts as the server side pipeline end. It attaches the peer connection info to the channel context of all insterested
 * handlers when the channel is connected. Each message it receives is forwarded to the appropiate finite state machine instance.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@Slf4j
@RequiredArgsConstructor
public class BGPv4ServerEndpoint extends SimpleChannelInboundHandler<Object>
{

  public static final String HANDLER_NAME = "BGP4-ServerEndpoint";

  private final FSMRegistry fsmRegistry;
  private final ChannelGroup trackedChannels = new DefaultChannelGroup(HANDLER_NAME, GlobalEventExecutor.INSTANCE);

  /*
   * (non-Javadoc)
   * 
   * @see org.jboss.netty.channel.SimpleChannelHandler#messageReceived(org.jboss.netty.channel.ChannelHandlerContext,
   * org.jboss.netty.channel.MessageEvent)
   */

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Object e) throws Exception
  {
    BGPv4FSM fsm = fsmRegistry.lookupFSM(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());

    if (fsm == null)
    {
      log.error("Internal Error: client for address {} is unknown", ctx.channel().remoteAddress());
      ctx.channel().close();
    }
    else
    {
      if (e instanceof BGPv4Packet)
      {
        fsm.handleMessage(ctx.channel(), (BGPv4Packet) e);
      }
      else
      {
        log.error("unknown payload class {} received for peer {}", e.getClass().getName(), ctx.channel().remoteAddress());
      }
    }
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception
  {

    final Channel clientChannel = ctx.channel();

    log.info("connected to client " + clientChannel.remoteAddress());

    BGPv4FSM fsm = fsmRegistry.lookupFSM(((InetSocketAddress) clientChannel.remoteAddress()).getAddress());

    if (fsm == null)
    {
      log.error("Internal Error: client for address " + clientChannel.remoteAddress() + " is unknown");
      clientChannel.close();
    }
    else if (fsm.isCanAcceptConnection())
    {

      ChannelPipeline pipeline = ctx.pipeline();
      PeerConnectionInformation pci = fsm.getPeerConnectionInformation();

      pipeline.forEach(e -> {

        ChannelHandler handler = e.getValue();

        if (handler.getClass().isAnnotationPresent(PeerConnectionInformationAware.class))
        {
          log.info("attaching peer connection information {} to handler {} for client {}", pci, e.getKey(), clientChannel.remoteAddress());
          pipeline.context(handler).attr(BGPv4ClientEndpoint.PEER_CONNECTION_INFO).set(pci);
        }

      });

      fsm.handleServerOpened(clientChannel);

      trackedChannels.add(clientChannel);
      ctx.fireChannelActive();

    }
    else
    {
      log.info("Connection from client {} cannot be accepted", clientChannel.remoteAddress());
      clientChannel.close();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jboss.netty.channel.SimpleChannelHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext,
   * org.jboss.netty.channel.ChannelStateEvent)
   */
  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception
  {

    log.info("closed connection to client " + ctx.channel().remoteAddress());

    BGPv4FSM fsm = fsmRegistry.lookupFSM(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());

    if (fsm == null)
    {
      log.error("Internal Error: client for address " + ctx.channel().remoteAddress() + " is unknown");
      ctx.channel().close();
    }
    else
    {
      fsm.handleClosed(ctx.channel());
      ctx.fireChannelUnregistered();
    }

    trackedChannels.remove(ctx.channel());

  }

  /**
   * @return the trackedChannels
   */
  public ChannelGroup getTrackedChannels()
  {
    return trackedChannels;
  }

}
