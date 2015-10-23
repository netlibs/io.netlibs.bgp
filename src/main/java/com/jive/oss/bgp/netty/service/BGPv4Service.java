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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import com.jive.oss.bgp.config.global.ApplicationConfiguration;
import com.jive.oss.bgp.config.nodes.impl.CapabilitiesImpl;
import com.jive.oss.bgp.config.nodes.impl.ClientConfigurationImpl;
import com.jive.oss.bgp.config.nodes.impl.PeerConfigurationImpl;
import com.jive.oss.bgp.net.ASType;
import com.jive.oss.bgp.net.AddressFamily;
import com.jive.oss.bgp.net.AddressFamilyKey;
import com.jive.oss.bgp.net.BinaryNextHop;
import com.jive.oss.bgp.net.InetAddressNextHop;
import com.jive.oss.bgp.net.NetworkLayerReachabilityInformation;
import com.jive.oss.bgp.net.NextHop;
import com.jive.oss.bgp.net.Origin;
import com.jive.oss.bgp.net.PathSegment;
import com.jive.oss.bgp.net.PathSegmentType;
import com.jive.oss.bgp.net.RIBSide;
import com.jive.oss.bgp.net.SubsequentAddressFamily;
import com.jive.oss.bgp.net.attributes.ASPathAttribute;
import com.jive.oss.bgp.net.attributes.MultiProtocolReachableNLRI;
import com.jive.oss.bgp.net.attributes.NextHopPathAttribute;
import com.jive.oss.bgp.net.attributes.OriginPathAttribute;
import com.jive.oss.bgp.net.attributes.PathAttribute;
import com.jive.oss.bgp.net.capabilities.Capability;
import com.jive.oss.bgp.net.capabilities.MultiProtocolCapability;
import com.jive.oss.bgp.netty.fsm.FSMRegistry;
import com.jive.oss.bgp.netty.fsm.OutboundRoutingUpdateQueue;
import com.jive.oss.bgp.rib.PeerRoutingInformationBase;
import com.jive.oss.bgp.rib.PeerRoutingInformationBaseManager;
import com.jive.oss.bgp.rib.Route;
import com.jive.oss.bgp.rib.RouteAdded;
import com.jive.oss.bgp.rib.RouteWithdrawn;
import com.jive.oss.bgp.rib.RoutingEventListener;
import com.jive.oss.bgp.rib.RoutingInformationBase;

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

    final Collection<PathAttribute> pathAttributes = new LinkedList<>();

    // ----

    final List<PathSegment> segs = new LinkedList<>();
    segs.add(new PathSegment(ASType.AS_NUMBER_2OCTETS, PathSegmentType.AS_SEQUENCE, new int[] { 1234 }));

    pathAttributes.add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS, segs));
    pathAttributes.add(new NextHopPathAttribute((Inet4Address) InetAddress.getByName("192.168.13.1")));

    final NextHop nextHop = new InetAddressNextHop<InetAddress>(InetAddress.getByName("192.168.13.1"));

    pathAttributes.add(new OriginPathAttribute(Origin.INCOMPLETE));

    pathAttributes.add(new MultiProtocolReachableNLRI(
        AddressFamily.IPv4,
        SubsequentAddressFamily.NLRI_UNICAST_WITH_MPLS_FORWARDING,
        new BinaryNextHop(new byte[] { 8, 1, 1, 1, 1, 2, 3, 4, 32 })));

    // ----

    final NetworkLayerReachabilityInformation nlri = new NetworkLayerReachabilityInformation(56, new byte[] { 0, 2, 1, 2, 3, 4, 5 });
    final Route route = new Route(AddressFamilyKey.IPV4_UNICAST_MPLS_FORWARDING, nlri, pathAttributes, new BinaryNextHop(InetAddress.getByName("192.168.13.1").getAddress()));

    // ----

    final PeerRoutingInformationBase prib = pribm.peerRoutingInformationBase("test");
    prib.allocateRoutingInformationBase(RIBSide.Local, AddressFamilyKey.IPV4_UNICAST_MPLS_FORWARDING);
    prib.allocateRoutingInformationBase(RIBSide.Remote, AddressFamilyKey.IPV4_UNICAST_MPLS_FORWARDING);
    final RoutingInformationBase rib = prib.routingBase(RIBSide.Local, AddressFamilyKey.IPV4_UNICAST_MPLS_FORWARDING);

    rib.addRoute(route);

    prib.routingBase(RIBSide.Remote, AddressFamilyKey.IPV4_UNICAST_MPLS_FORWARDING).addPerRibListener(new RoutingEventListener() {

      @Override
      public void routeWithdrawn(RouteWithdrawn event)
      {
        System.err.println(event);
      }

      @Override
      public void routeAdded(RouteAdded event)
      {
        System.err.println(event);
      }
      
    });

    // ----

    final ClientConfigurationImpl clientConfig = new ClientConfigurationImpl(new InetSocketAddress("192.168.13.129", 179));

    final PeerConfigurationImpl config = new PeerConfigurationImpl("test", clientConfig, 1234, 5678, 1, get(InetAddress.getByName("192.168.13.129")));

    final CapabilitiesImpl caps = new CapabilitiesImpl(new Capability[] {
        // new AutonomousSystem4Capability(16),
        new MultiProtocolCapability(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING),
        new MultiProtocolCapability(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_WITH_MPLS_FORWARDING),
        // new RouteRefreshCapability()
    });

    config.setCapabilities(caps);

    final ApplicationConfiguration app = new ApplicationConfiguration();
    app.addPeer(config);
    app.setPeerRoutingInformationBaseManager(pribm);

    final FSMRegistry reg = new FSMRegistry(app, scheduler);

    final BGPv4Service service = new BGPv4Service(reg, new BGPv4Server(app, reg), scheduler);

    // ----

    service.startService();

    Thread.sleep(1000);

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
