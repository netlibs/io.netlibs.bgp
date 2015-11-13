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
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
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
import com.jive.oss.bgp.net.attributes.MultiExitDiscPathAttribute;
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
import com.jive.oss.commons.ip.CidrV4Address;
import com.jive.oss.commons.ip.CidrV4AddressTest;

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

    // ----

    // this is the Adj-RIB-In and Adj-RIB-Out construct
    final PeerRoutingInformationBase prib = pribm.peerRoutingInformationBase("test-v4UnicastRIB");

    List<AddressFamilyKey> enabledAfis = new ArrayList<AddressFamilyKey>() {{ 
        add(AddressFamilyKey.IPV4_UNICAST_FORWARDING);
        add(AddressFamilyKey.IPV4_UNICAST_MPLS_FORWARDING);
    }};
    
    for (AddressFamilyKey afi : enabledAfis){
      prib.allocateRoutingInformationBase(RIBSide.Local, afi);
      prib.allocateRoutingInformationBase(RIBSide.Remote, afi);
    }
   
    
    // ---------------------------- IPv6 LABELLED UNICAST ----------------------------------------------//

    /// IPV4 LABELLED UNICAST ADVERTISE CODE
    final Collection<PathAttribute> v4lPathAttributes = new LinkedList<>();
    final List<PathSegment> v4lSegm = new LinkedList<>();
    v4lSegm.add(new PathSegment(ASType.AS_NUMBER_2OCTETS, PathSegmentType.AS_SEQUENCE, new int[] { 1234 }));
    v4lPathAttributes.add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS, v4lSegm));
    v4lPathAttributes.add(new OriginPathAttribute(Origin.IGP));
    v4lPathAttributes.add(new MultiProtocolReachableNLRI(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_WITH_MPLS_FORWARDING));
    IPv4MPLSLabelNLRI v4lNlri = IPv4MPLSLabelNLRI.fromCidrV4AddressAndLabel(CidrV4Address.fromString("172.12.13.0/24"), 256, true);
    Route v4lroute = new Route(AddressFamilyKey.IPV4_UNICAST_MPLS_FORWARDING, v4lNlri.getEncodedNLRI(), v4lPathAttributes, new BinaryNextHop(InetAddress.getByName("192.168.207.1").getAddress()));
    prib.routingBase(RIBSide.Local, AddressFamilyKey.IPV4_UNICAST_MPLS_FORWARDING).addRoute(v4lroute);
    
    /// IPV4 LABELLED UNICAST LISTEN CODE
    RoutingEventListener adjRIBv4LabelledUni = new RoutingEventListener() {
      @Override
      public void routeAdded(final RouteAdded event){
        Route rt = event.getRoute();
        IPv4MPLSLabelNLRI nlri = new IPv4MPLSLabelNLRI(rt.getNlri().getPrefix());
        try
        {
          System.err.printf("received UPDATE for %s/%s -> lbl %s\n", nlri.getInetAddress(), nlri.getAddress().getPrefixLength(), nlri.getLabel());
        }
        catch (UnknownHostException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      @Override
      public void routeWithdrawn(RouteWithdrawn event)
      {
        System.err.println("routeWithdrawn Called");
      }
    };
    prib.routingBase(RIBSide.Remote, AddressFamilyKey.IPV4_UNICAST_MPLS_FORWARDING).addPerRibListener(adjRIBv4LabelledUni);
    
    // ---------------------------- IPv4 UNICAST ----------------------------------------------//
    
    // IPv4 UNICAST ADVERTISE CODE
    final Collection<PathAttribute> v4PathAttributes = new LinkedList<>();
    final List<PathSegment> v4Segm = new LinkedList<>();
    v4Segm.add(new PathSegment(ASType.AS_NUMBER_2OCTETS, PathSegmentType.AS_SEQUENCE, new int[] { 1234 }));
    v4PathAttributes.add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS, v4Segm));
    v4PathAttributes.add(new OriginPathAttribute(Origin.INCOMPLETE));
    IPv4UnicastNLRI v4Nlri = IPv4UnicastNLRI.fromCidrV4Address(CidrV4Address.fromString("172.13.1.2/32"));
    Route v4route = new Route(AddressFamilyKey.IPV4_UNICAST_FORWARDING, v4Nlri.getEncodedNlri(), v4PathAttributes, new InetAddressNextHop<InetAddress>(InetAddress.getByName("192.168.207.1")));
    prib.routingBase(RIBSide.Local, AddressFamilyKey.IPV4_UNICAST_FORWARDING).addRoute(v4route);
    
    /// IPV4 UNICAST LISTEN CODE
    RoutingEventListener adjRIBv4Uni = new RoutingEventListener() {
      @Override
      public void routeAdded(final RouteAdded event){
        Route rt = event.getRoute();
        IPv4UnicastNLRI nlri = new IPv4UnicastNLRI(rt.getNlri().getPrefix());
        try
        {
          System.err.printf("received UPDATE for %s/%s.\n", nlri.getInetAddress(), nlri.getAddress().getPrefixLength());
        }
        catch (UnknownHostException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

      @Override
      public void routeWithdrawn(RouteWithdrawn event)
      {
        System.err.println("routeWithdrawn Called");
      }
    };
    prib.routingBase(RIBSide.Remote, AddressFamilyKey.IPV4_UNICAST_FORWARDING).addPerRibListener(adjRIBv4Uni);
    
    
    final ClientConfigurationImpl clientConfig = new ClientConfigurationImpl(new InetSocketAddress("192.168.207.130", 179));
    
    final PeerConfigurationImpl config = new PeerConfigurationImpl("test-v4UnicastRIB", clientConfig, 1234, 5678, 1, get(InetAddress.getByName("192.168.207.130")));

    final CapabilitiesImpl caps = new CapabilitiesImpl(new Capability[] {
        new MultiProtocolCapability(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING),
        new MultiProtocolCapability(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_WITH_MPLS_FORWARDING),
        new MultiProtocolCapability(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_MPLS_LABELLED_VPN),
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

    while (true)
    {
      Thread.sleep(1000);
      
    }

  }

  private static long get(final InetAddress a)
  {
    byte[] buf = a.getAddress();
    byte[] pad = { 0, 0, 0, 0 };
    ByteBuffer target = ByteBuffer.allocate(Long.BYTES);
    // pad the buffer with 4-bytes of 0s
    target.put(pad);
    // put the 32-bits of router-ID here
    target.put(buf);
    // return to position 0
    target.flip();
    return target.getLong();
  }

}
