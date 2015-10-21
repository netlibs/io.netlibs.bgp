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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.bgp4j.config.nodes.ClientConfiguration;
import org.bgp4j.config.nodes.PeerConfiguration;
import org.bgp4j.config.nodes.impl.ClientConfigurationImpl;
import org.bgp4j.config.nodes.impl.PeerConfigurationImpl;
import org.bgp4j.netty.fsm.BGPv4FSM;
import org.bgp4j.netty.fsm.CapabilitesNegotiator;
import org.bgp4j.netty.fsm.FSMRegistry;
import org.bgp4j.netty.fsm.OutboundRoutingUpdateQueue;
import org.bgp4j.netty.handlers.BGPv4ClientEndpoint;
import org.bgp4j.netty.handlers.BGPv4Codec;
import org.bgp4j.netty.handlers.BGPv4Reframer;
import org.bgp4j.netty.handlers.InboundOpenCapabilitiesProcessor;
import org.bgp4j.netty.handlers.ValidateServerIdentifier;
import org.bgp4j.netty.protocol.BGPv4PacketDecoder;
import org.bgp4j.netty.protocol.open.OpenPacketDecoder;
import org.bgp4j.netty.protocol.refresh.RouteRefreshPacketDecoder;
import org.bgp4j.netty.protocol.update.UpdatePacketDecoder;
import org.bgp4j.rib.PeerRoutingInformationBaseManager;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@Slf4j
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
        ch.pipeline().addLast(BGPv4Codec.HANDLER_NAME, new BGPv4Codec(new BGPv4PacketDecoder(new OpenPacketDecoder(), new UpdatePacketDecoder(), new RouteRefreshPacketDecoder())));
        ch.pipeline().addLast(InboundOpenCapabilitiesProcessor.HANDLER_NAME, new InboundOpenCapabilitiesProcessor());
        ch.pipeline().addLast(ValidateServerIdentifier.HANDLER_NAME, new ValidateServerIdentifier());
        ch.pipeline().addLast(BGPv4ClientEndpoint.HANDLER_NAME, new BGPv4ClientEndpoint(registry));
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

  public static void main(final String[] args) throws Exception
  {

    final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
    
    scheduler.start();
    
    final PeerRoutingInformationBaseManager pribm = new PeerRoutingInformationBaseManager();
    final FSMRegistry reg = new FSMRegistry();
    final OutboundRoutingUpdateQueue out = new OutboundRoutingUpdateQueue(scheduler);

    final BGPv4Client client = new BGPv4Client(reg);
    final ClientConfiguration clientConfig = new ClientConfigurationImpl(new InetSocketAddress("192.168.13.129", 179));
    final PeerConfiguration config = new PeerConfigurationImpl("test", clientConfig, 1234, 5678, 1, get(Inet4Address.getByName("192.168.13.129")));

    final BGPv4FSM fsm = new BGPv4FSM(scheduler, client, new CapabilitesNegotiator(), pribm, out);
    fsm.configure(config);

    reg.registerFSM(fsm);

    reg.startFiniteStateMachines();
    // client.startClient(config).sync();

    while (true)
    {
      Thread.sleep(1000);
    }

  }

  private static long get(InetAddress a)
  {
    byte[] b = a.getAddress();
    long i = 3232238977L;
    return i;
  }

}
