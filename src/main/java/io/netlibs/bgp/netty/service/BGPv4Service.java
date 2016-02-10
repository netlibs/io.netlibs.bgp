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
package io.netlibs.bgp.netty.service;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import com.google.common.net.InetAddresses;

import io.netlibs.bgp.config.global.ApplicationConfiguration;
import io.netlibs.bgp.config.nodes.impl.CapabilitiesImpl;
import io.netlibs.bgp.config.nodes.impl.ClientConfigurationImpl;
import io.netlibs.bgp.config.nodes.impl.PeerConfigurationImpl;
import io.netlibs.bgp.net.ASType;
import io.netlibs.bgp.net.AddressFamily;
import io.netlibs.bgp.net.AddressFamilyKey;
import io.netlibs.bgp.net.BinaryNextHop;
import io.netlibs.bgp.net.InetAddressNextHop;
import io.netlibs.bgp.net.Origin;
import io.netlibs.bgp.net.PathSegment;
import io.netlibs.bgp.net.PathSegmentType;
import io.netlibs.bgp.net.RIBSide;
import io.netlibs.bgp.net.RouteDistinguisherType0;
import io.netlibs.bgp.net.SubsequentAddressFamily;
import io.netlibs.bgp.net.TransitiveIPv4AddressTwoByteAdministratorRT;
import io.netlibs.bgp.net.TransitiveTwoByteASNFourByteAdministratorRT;
import io.netlibs.bgp.net.attributes.ASPathAttribute;
import io.netlibs.bgp.net.attributes.AbstractExtendedCommunityInterface;
import io.netlibs.bgp.net.attributes.ExtendedCommunityPathAttribute;
import io.netlibs.bgp.net.attributes.IPv4MPLSLabelNLRI;
import io.netlibs.bgp.net.attributes.IPv4MPLSVPNNLRI;
import io.netlibs.bgp.net.attributes.IPv4UnicastNLRI;
import io.netlibs.bgp.net.attributes.IPv6MPLSLabelNLRI;
import io.netlibs.bgp.net.attributes.IPv6MPLSVPNNLRI;
import io.netlibs.bgp.net.attributes.IPv6UnicastNLRI;
import io.netlibs.bgp.net.attributes.MultiProtocolReachableNLRI;
import io.netlibs.bgp.net.attributes.OriginPathAttribute;
import io.netlibs.bgp.net.attributes.PathAttribute;
import io.netlibs.bgp.net.capabilities.Capability;
import io.netlibs.bgp.net.capabilities.MultiProtocolCapability;
import io.netlibs.bgp.netty.fsm.FSMRegistry;
import io.netlibs.bgp.netty.fsm.OutboundRoutingUpdateQueue;
import io.netlibs.bgp.rib.PeerRoutingInformationBase;
import io.netlibs.bgp.rib.PeerRoutingInformationBaseManager;
import io.netlibs.bgp.rib.Route;
import io.netlibs.bgp.rib.RouteAdded;
import io.netlibs.bgp.rib.RouteWithdrawn;
import io.netlibs.bgp.rib.RoutingEventListener;
import io.netlibs.ipaddr.CidrV4Address;
import io.netlibs.ipaddr.CidrV6Address;
import io.netlibs.ipaddr.IPv6Address;
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
        add(AddressFamilyKey.IPV4_MPLS_VPN_FORWARDING);
        add(AddressFamilyKey.IPV6_UNICAST_FORWARDING);
        add(AddressFamilyKey.IPV6_UNICAST_MPLS_FORWARDING);
        add(AddressFamilyKey.IPV6_MPLS_VPN_FORWARDING);
    }};
    
    for (AddressFamilyKey afi : enabledAfis){
      prib.allocateRoutingInformationBase(RIBSide.Local, afi);
      prib.allocateRoutingInformationBase(RIBSide.Remote, afi);
    }
   
    
    // ---------------------------- IPv4 LABELLED UNICAST ----------------------------------------------//

    /// IPV4 LABELLED UNICAST ADVERTISE CODE
    final Collection<PathAttribute> v4lPathAttributes = new LinkedList<>();
    final List<PathSegment> v4lSegm = new LinkedList<>();
    v4lSegm.add(new PathSegment(ASType.AS_NUMBER_2OCTETS, PathSegmentType.AS_SEQUENCE, new int[] { 1234 }));
    v4lPathAttributes.add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS, v4lSegm));
    v4lPathAttributes.add(new OriginPathAttribute(Origin.EGP));
    v4lPathAttributes.add(new MultiProtocolReachableNLRI(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_WITH_MPLS_FORWARDING));
    IPv4MPLSLabelNLRI v4lNlri = IPv4MPLSLabelNLRI.fromCidrV4AddressAndLabel(CidrV4Address.fromString("172.12.13.0/24"), 408392, true);
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
        Route rt = event.getRoute();
        IPv4MPLSLabelNLRI nlri = new IPv4MPLSLabelNLRI(rt.getNlri().getPrefix());
        try
        {
          System.err.printf("WITHDRAW received UPDATE for %s/%s -> lbl %s\n", nlri.getInetAddress(), nlri.getAddress().getPrefixLength(), nlri.getLabel());
        }
        catch (UnknownHostException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    };
    prib.routingBase(RIBSide.Remote, AddressFamilyKey.IPV4_UNICAST_MPLS_FORWARDING).addPerRibListener(adjRIBv4LabelledUni);
    
    // ---------------------------- IPv4 UNICAST ----------------------------------------------//
    
    // IPv4 UNICAST ADVERTISE CODE
    final Collection<PathAttribute> v4PathAttributes = new LinkedList<>();
    final List<PathSegment> v4Segm = new LinkedList<>();
    v4Segm.add(new PathSegment(ASType.AS_NUMBER_2OCTETS, PathSegmentType.AS_SEQUENCE, new int[] { 1234 }));
    v4PathAttributes.add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS, v4Segm));
    v4PathAttributes.add(new OriginPathAttribute(Origin.EGP));
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
        Route rt = event.getRoute();
        IPv4UnicastNLRI nlri = new IPv4UnicastNLRI(rt.getNlri().getPrefix());
        try
        {
          System.err.printf("WITHDRAW received UPDATE for %s/%s.\n", nlri.getInetAddress(), nlri.getAddress().getPrefixLength());
        }
        catch (UnknownHostException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    };
    prib.routingBase(RIBSide.Remote, AddressFamilyKey.IPV4_UNICAST_FORWARDING).addPerRibListener(adjRIBv4Uni);

    
    // ---------------------------- VPNv4 ----------------------------------------------//
    
    // VPNv4 UNICAST ADVERTISE CODE
    final Collection<PathAttribute> vpn4PathAttributes = new LinkedList<>();
    final List<PathSegment> vpn4Segm = new LinkedList<>();
    vpn4Segm.add(new PathSegment(ASType.AS_NUMBER_2OCTETS, PathSegmentType.AS_SEQUENCE, new int[] { 1234 }));
    vpn4PathAttributes.add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS, v4Segm));
    vpn4PathAttributes.add(new OriginPathAttribute(Origin.EGP));
    vpn4PathAttributes.add(new MultiProtocolReachableNLRI(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_MPLS_LABELLED_VPN));
    IPv4MPLSVPNNLRI vpn4Nlri = IPv4MPLSVPNNLRI.fromCidrV4AddressRDAndLabel(CidrV4Address.fromString("192.168.0.0/31"), new RouteDistinguisherType0(6643, 4421), 400);
    final List<AbstractExtendedCommunityInterface> v4extComms = new LinkedList<>();
    v4extComms.add(new TransitiveTwoByteASNFourByteAdministratorRT(6643, 10L));
    v4extComms.add(new TransitiveIPv4AddressTwoByteAdministratorRT((Inet4Address) InetAddresses.forString("14.15.27.32"), 4231));
    vpn4PathAttributes.add(new ExtendedCommunityPathAttribute(v4extComms));
   
    Route vpn4route = new Route(AddressFamilyKey.IPV4_MPLS_VPN_FORWARDING, vpn4Nlri.getEncodedNLRI(), vpn4PathAttributes, BinaryNextHop.fromRDandNextHop(new RouteDistinguisherType0(0, 0), InetAddress.getByName("192.168.207.1")));
    prib.routingBase(RIBSide.Local, AddressFamilyKey.IPV4_MPLS_VPN_FORWARDING).addRoute(vpn4route);
    
    /// VPNv4 UNICAST LISTEN CODE
    RoutingEventListener adjRIBvpn4Uni = new RoutingEventListener() {
      @Override
      public void routeAdded(final RouteAdded event){
        Route rt = event.getRoute();
        IPv4MPLSVPNNLRI nlri = null;
        try
        {
          nlri = new IPv4MPLSVPNNLRI(rt.getNlri().getPrefix());
        }
        catch (UnknownHostException e1)
        {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        try
        {
          if (nlri != null)
            System.err.printf("received update for %s:%s/%s -> lbl %d\n", nlri.getRd().humanReadable(), nlri.getInetAddress(), nlri.getNlri().getPrefixLength(), nlri.getLabel());
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
        Route rt = event.getRoute();
        IPv4MPLSVPNNLRI nlri = null;
        try
        {
          nlri = new IPv4MPLSVPNNLRI(rt.getNlri().getPrefix());
        }
        catch (UnknownHostException e1)
        {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        try
        {
          if (nlri != null)
            System.err.printf("WITHDRAW received update for %s:%s/%s -> lbl %d\n", nlri.getRd().humanReadable(), nlri.getInetAddress(), nlri.getNlri().getPrefixLength(), nlri.getLabel());
        }
        catch (UnknownHostException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    };
    prib.routingBase(RIBSide.Remote, AddressFamilyKey.IPV4_MPLS_VPN_FORWARDING).addPerRibListener(adjRIBvpn4Uni);
 
    // ---------------------------- IPv6 UNICAST ----------------------------------------------//
    
    // IPv6 UNICAST ADVERTISE CODE
    final Collection<PathAttribute> v6PathAttributes = new LinkedList<>();
    final List<PathSegment> v6Segm = new LinkedList<>();
    v6Segm.add(new PathSegment(ASType.AS_NUMBER_2OCTETS, PathSegmentType.AS_SEQUENCE, new int[] { 1234 }));
    v6PathAttributes.add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS, v4Segm));
    v6PathAttributes.add(new OriginPathAttribute(Origin.EGP));
    v6PathAttributes.add(new MultiProtocolReachableNLRI(AddressFamily.IPv6, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING));
    IPv6UnicastNLRI v6Nlri = IPv6UnicastNLRI.fromCidrV6Address(CidrV6Address.fromString("2001:db8::/32"));
    Route v6route = new Route(AddressFamilyKey.IPV6_UNICAST_FORWARDING, v6Nlri.getEncodedNlri(), v6PathAttributes, new BinaryNextHop(IPv6Address.fromString("2001:4c20::1").toByteArray()));
    prib.routingBase(RIBSide.Local, AddressFamilyKey.IPV6_UNICAST_FORWARDING).addRoute(v6route);
    
    /// IPV6 UNICAST LISTEN CODE
    RoutingEventListener adjRIBv6Uni = new RoutingEventListener() {
      @Override
      public void routeAdded(final RouteAdded event){
        Route rt = event.getRoute();
        IPv6UnicastNLRI nlri = new IPv6UnicastNLRI(rt.getNlri().getPrefix());
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
        Route rt = event.getRoute();
        IPv6UnicastNLRI nlri = new IPv6UnicastNLRI(rt.getNlri().getPrefix());
        try
        {
          System.err.printf("WITHDRAW received UPDATE for %s/%s.\n", nlri.getInetAddress(), nlri.getAddress().getPrefixLength());
        }
        catch (UnknownHostException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    };
    prib.routingBase(RIBSide.Remote, AddressFamilyKey.IPV6_UNICAST_FORWARDING).addPerRibListener(adjRIBv6Uni);    

    // ---------------------------- IPv6 LABELLED UNICAST ----------------------------------------------//
    
    // IPv6 LABELLED UNICAST ADVERTISE CODE
    final Collection<PathAttribute> v6lPathAttributes = new LinkedList<>();
    final List<PathSegment> v6lSegm = new LinkedList<>();
    v6lSegm.add(new PathSegment(ASType.AS_NUMBER_2OCTETS, PathSegmentType.AS_SEQUENCE, new int[] { 1234 }));
    v6lPathAttributes.add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS, v4Segm));
    v6lPathAttributes.add(new OriginPathAttribute(Origin.EGP));
    v6lPathAttributes.add(new MultiProtocolReachableNLRI(AddressFamily.IPv6, SubsequentAddressFamily.NLRI_UNICAST_WITH_MPLS_FORWARDING));
    IPv6MPLSLabelNLRI v6lNlri = IPv6MPLSLabelNLRI.fromCidrV6AddressAndLabel(CidrV6Address.fromString("2001:4c22:1648::/48"), 509728, true);
    Route v6lroute = new Route(AddressFamilyKey.IPV6_UNICAST_MPLS_FORWARDING, v6lNlri.getEncodedNLRI(), v6lPathAttributes, new BinaryNextHop(IPv6Address.fromString("2001:4c20::42").toByteArray()));
    prib.routingBase(RIBSide.Local, AddressFamilyKey.IPV6_UNICAST_MPLS_FORWARDING).addRoute(v6lroute);
    
    /// IPV6 UNICAST LISTEN CODE
    RoutingEventListener adjRIBv6lUni = new RoutingEventListener() {
      @Override
      public void routeAdded(final RouteAdded event){
        Route rt = event.getRoute();
        IPv6MPLSLabelNLRI nlri = new IPv6MPLSLabelNLRI(rt.getNlri().getPrefix());
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
        Route rt = event.getRoute();
        IPv6MPLSLabelNLRI nlri = new IPv6MPLSLabelNLRI(rt.getNlri().getPrefix());
        try
        {
          System.err.printf("WITHDRAW received UPDATE for %s/%s.\n", nlri.getInetAddress(), nlri.getAddress().getPrefixLength());
        }
        catch (UnknownHostException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    };
    prib.routingBase(RIBSide.Remote, AddressFamilyKey.IPV6_UNICAST_MPLS_FORWARDING).addPerRibListener(adjRIBv6lUni);        

    
    // ---------------------------- VPNv6 ----------------------------------------------//
    
    // VPNv6 UNICAST ADVERTISE CODE
    final Collection<PathAttribute> vpn6PathAttributes = new LinkedList<>();
    final List<PathSegment> vpn6Segm = new LinkedList<>();
    vpn6Segm.add(new PathSegment(ASType.AS_NUMBER_2OCTETS, PathSegmentType.AS_SEQUENCE, new int[] { 1234 }));
    vpn6PathAttributes.add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS, v4Segm));
    vpn6PathAttributes.add(new OriginPathAttribute(Origin.EGP));
    vpn6PathAttributes.add(new MultiProtocolReachableNLRI(AddressFamily.IPv6, SubsequentAddressFamily.NLRI_MPLS_LABELLED_VPN));
    IPv6MPLSVPNNLRI vpn6Nlri = IPv6MPLSVPNNLRI.fromCidrV6AddressRDAndLabel(CidrV6Address.fromString("2a02:46::/32"), new RouteDistinguisherType0(6643, 4421), 422);
    final List<AbstractExtendedCommunityInterface> v6extComms = new LinkedList<>();
    v6extComms.add(new TransitiveTwoByteASNFourByteAdministratorRT(6643, 10L));
    v6extComms.add(new TransitiveIPv4AddressTwoByteAdministratorRT((Inet4Address) InetAddresses.forString("43.23.12.24"), 4231));
    vpn6PathAttributes.add(new ExtendedCommunityPathAttribute(v6extComms));
       
    Route vpn6route = new Route(AddressFamilyKey.IPV6_MPLS_VPN_FORWARDING, vpn6Nlri.getEncodedNLRI(), vpn6PathAttributes, BinaryNextHop.fromRDandNextHop(new RouteDistinguisherType0(0, 0), InetAddress.getByName("2001:4c20::2")));
    prib.routingBase(RIBSide.Local, AddressFamilyKey.IPV6_MPLS_VPN_FORWARDING).addRoute(vpn6route);
    
    /// VPNv6 UNICAST LISTEN CODE
    RoutingEventListener adjRIBvpn6Uni = new RoutingEventListener() {
      @Override
      public void routeAdded(final RouteAdded event){
        Route rt = event.getRoute();
        IPv6MPLSVPNNLRI nlri = null;
        try
        {
          nlri = new IPv6MPLSVPNNLRI(rt.getNlri().getPrefix());
        }
        catch (UnknownHostException e1)
        {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        try
        {
          if (nlri != null)
            System.err.printf("received update for %s:%s/%s lbl %s\n", nlri.getRd().humanReadable(), nlri.getInetAddress(), nlri.getNlri().getPrefixLength(), nlri.getLabel());
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
        Route rt = event.getRoute();
        IPv6MPLSVPNNLRI nlri = null;
        try
        {
          nlri = new IPv6MPLSVPNNLRI(rt.getNlri().getPrefix());
        }
        catch (UnknownHostException e1)
        {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        try
        {
          if (nlri != null)
            System.err.printf("WITHDRAW received update for %s:%s/%s lbl %s\n", nlri.getRd().humanReadable(), nlri.getInetAddress(), nlri.getNlri().getPrefixLength(), nlri.getLabel());
        }
        catch (UnknownHostException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    };
    prib.routingBase(RIBSide.Remote, AddressFamilyKey.IPV6_MPLS_VPN_FORWARDING).addPerRibListener(adjRIBvpn6Uni);    
    
    final ClientConfigurationImpl clientConfig = new ClientConfigurationImpl(new InetSocketAddress("192.168.207.130", 179));
    
    final PeerConfigurationImpl config = new PeerConfigurationImpl("test-v4UnicastRIB", clientConfig, 1234, 5678, 1, get(InetAddress.getByName("192.168.207.130")));

    final CapabilitiesImpl caps = new CapabilitiesImpl(new Capability[] {
       new MultiProtocolCapability(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING),
       //new MultiProtocolCapability(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_WITH_MPLS_FORWARDING),
       //new MultiProtocolCapability(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_MPLS_LABELLED_VPN),
        new MultiProtocolCapability(AddressFamily.IPv6, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING),
       //new MultiProtocolCapability(AddressFamily.IPv6, SubsequentAddressFamily.NLRI_UNICAST_WITH_MPLS_FORWARDING),
       //new MultiProtocolCapability(AddressFamily.IPv6, SubsequentAddressFamily.NLRI_MPLS_LABELLED_VPN),
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
