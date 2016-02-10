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
package io.joss.bgp.netty.service;

import io.joss.bgp.config.nodes.PeerConfiguration;
import io.joss.bgp.netty.codec.BGPv4PacketDecoder;
import io.joss.bgp.netty.fsm.FSMRegistry;
import io.joss.bgp.netty.handlers.BGPv4ClientEndpoint;
import io.joss.bgp.netty.handlers.BGPv4Codec;
import io.joss.bgp.netty.handlers.BGPv4Reframer;
import io.joss.bgp.netty.handlers.InboundOpenCapabilitiesProcessor;
import io.joss.bgp.netty.handlers.ValidateServerIdentifier;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

public class BGPv4Client
{

  private static final EventLoopGroup workerGroup = new NioEventLoopGroup(1);
  private Channel clientChannel;
  private final FSMRegistry registry;

  public BGPv4Client(final FSMRegistry registry)
  {
    this.registry = registry;
  }

  public ChannelFuture startClient(final PeerConfiguration peerConfiguration)
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
        ch.pipeline().addLast(BGPv4Codec.HANDLER_NAME, new BGPv4Codec(BGPv4PacketDecoder.getInstance()));
        ch.pipeline().addLast(InboundOpenCapabilitiesProcessor.HANDLER_NAME, new InboundOpenCapabilitiesProcessor());
        ch.pipeline().addLast(ValidateServerIdentifier.HANDLER_NAME, new ValidateServerIdentifier());
        ch.pipeline().addLast(BGPv4ClientEndpoint.HANDLER_NAME, new BGPv4ClientEndpoint(BGPv4Client.this.registry));
      }

    });

    // Start the client.
    return b.connect(peerConfiguration.getClientConfig().getRemoteAddress());

  }

  public void stopClient()
  {
    if (this.clientChannel != null)
    {
      this.clientChannel.close();
      this.clientChannel = null;
    }
  }

  /**
   * @return the clientChannel
   */

  public Channel getClientChannel()
  {
    return this.clientChannel;
  }


}
