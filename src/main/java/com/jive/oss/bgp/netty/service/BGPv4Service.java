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
package com.jive.oss.bgp.netty.service;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import com.jive.oss.bgp.config.global.ApplicationConfiguration;
import com.jive.oss.bgp.config.nodes.ClientConfiguration;
import com.jive.oss.bgp.config.nodes.PeerConfiguration;
import com.jive.oss.bgp.config.nodes.impl.ClientConfigurationImpl;
import com.jive.oss.bgp.config.nodes.impl.PeerConfigurationImpl;
import com.jive.oss.bgp.netty.fsm.FSMRegistry;
import com.jive.oss.bgp.netty.fsm.OutboundRoutingUpdateQueue;
import com.jive.oss.bgp.rib.PeerRoutingInformationBaseManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@Slf4j
@RequiredArgsConstructor
public class BGPv4Service
{

  private final FSMRegistry fsmRegistry;
  private final BGPv4Server serverInstance;
  private final Scheduler scheduler;

  /**
   * start the service
   * 
   * @param configuration
   *          the initial service configuration
   */
  public void startService()
  {

    this.fsmRegistry.createRegistry();

    if (this.serverInstance != null)
    {
      log.info("starting local BGPv4 server");
      this.serverInstance.startServer();
    }

    this.fsmRegistry.startFiniteStateMachines();
  }

  /**
   * stop the running service
   * 
   */
  public void stopService()
  {
    this.fsmRegistry.stopFiniteStateMachines();

    if (this.serverInstance != null)
    {
      this.serverInstance.stopServer();
    }

    this.fsmRegistry.destroyRegistry();
  }

  public static void main(final String[] args) throws Exception
  {

    final Scheduler scheduler = new StdSchedulerFactory().getScheduler();

    scheduler.start();

    final PeerRoutingInformationBaseManager pribm = new PeerRoutingInformationBaseManager();

    final OutboundRoutingUpdateQueue out = new OutboundRoutingUpdateQueue(scheduler);

    final ClientConfiguration clientConfig = new ClientConfigurationImpl(new InetSocketAddress("192.168.13.129", 179));
    final PeerConfiguration config = new PeerConfigurationImpl("test", clientConfig, 1234, 5678, 1, get(InetAddress.getByName("192.168.13.129")));

    ApplicationConfiguration app = new ApplicationConfiguration();
    app.addPeer(config);

    final FSMRegistry reg = new FSMRegistry(app, scheduler);

    BGPv4Service service = new BGPv4Service(reg, new BGPv4Server(app, reg), scheduler);

    service.startService();

    // final BGPv4FSM fsm = new BGPv4FSM(scheduler, client, new CapabilitesNegotiator(), pribm, out);
    // fsm.configure(config);
    //
    // reg.registerFSM(fsm);
    //
    // reg.startFiniteStateMachines();
    // client.startClient(config).sync();

    while (true)
    {
      Thread.sleep(1000);
    }

  }

  private static long get(final InetAddress a)
  {
    final byte[] b = a.getAddress();
    final long i = 16843266L;
    return i;
  }

}
