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
package org.bgp4j.netty.service;

import org.bgp4j.config.global.ApplicationConfiguration;
import org.bgp4j.netty.codec.BGPv4PacketDecoder;
import org.bgp4j.netty.fsm.FSMRegistry;
import org.bgp4j.netty.handlers.BGPv4Codec;
import org.bgp4j.netty.handlers.BGPv4Reframer;
import org.bgp4j.netty.handlers.BGPv4ServerEndpoint;
import org.bgp4j.netty.handlers.InboundOpenCapabilitiesProcessor;
import org.bgp4j.netty.handlers.ValidateServerIdentifier;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@Slf4j
public class BGPv4Server
{

  private final ApplicationConfiguration applicationConfiguration;
  private final BGPv4ServerEndpoint serverEndpoint;
  private BGPv4Codec codec = new BGPv4Codec(BGPv4PacketDecoder.getInstance());
  private InboundOpenCapabilitiesProcessor inboundOpenCapProcessor = new InboundOpenCapabilitiesProcessor();
  private ValidateServerIdentifier validateServer = new ValidateServerIdentifier();
  private BGPv4Reframer reframer = new BGPv4Reframer();
  private Channel serverChannel;

  public BGPv4Server(ApplicationConfiguration config, FSMRegistry registry)
  {
    this.serverEndpoint = new BGPv4ServerEndpoint(registry);
    this.applicationConfiguration = config;
  }

  public void startServer()
  {

    ServerBootstrap bootstrap = new ServerBootstrap();

    bootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup());

    bootstrap
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          public void initChannel(SocketChannel ch) throws Exception
          {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(BGPv4Reframer.HANDLER_NAME, BGPv4Server.this.reframer);
            pipeline.addLast(BGPv4Codec.HANDLER_NAME, BGPv4Server.this.codec);
            pipeline.addLast(InboundOpenCapabilitiesProcessor.HANDLER_NAME, BGPv4Server.this.inboundOpenCapProcessor);
            pipeline.addLast(ValidateServerIdentifier.HANDLER_NAME, BGPv4Server.this.validateServer);
            pipeline.addLast(BGPv4ServerEndpoint.HANDLER_NAME, BGPv4Server.this.serverEndpoint);
          }
        });

    bootstrap.option(ChannelOption.SO_BACKLOG, 128);

    bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
    bootstrap.childOption(ChannelOption.TCP_NODELAY, true);

    log.info("Starting local server");
    
    this.serverChannel = bootstrap.bind(applicationConfiguration.getServerPort()).syncUninterruptibly().channel();

  }

  public void stopServer()
  {

    log.info("closing all child connections");

    this.serverEndpoint.getTrackedChannels().close().awaitUninterruptibly();

    if (this.serverChannel != null)
    {
      log.info("stopping local server");
      this.serverChannel.close();
      this.serverChannel.closeFuture().awaitUninterruptibly();
    }

  }

}
