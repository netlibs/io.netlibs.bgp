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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.bgp4j.config.global.ApplicationConfiguration;
import org.bgp4j.config.nodes.ClientConfiguration;
import org.bgp4j.config.nodes.PeerConfiguration;
import org.bgp4j.config.nodes.impl.ClientConfigurationImpl;
import org.bgp4j.config.nodes.impl.PeerConfigurationImpl;
import org.bgp4j.netty.fsm.FSMRegistry;
import org.bgp4j.netty.fsm.OutboundRoutingUpdateQueue;
import org.bgp4j.rib.PeerRoutingInformationBaseManager;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

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

    this.fsmRegistry.createRegistry(scheduler);

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

    final FSMRegistry reg = new FSMRegistry(app);

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
