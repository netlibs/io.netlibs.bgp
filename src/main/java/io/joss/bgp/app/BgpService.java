package io.joss.bgp.app;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import io.joss.bgp.config.global.ApplicationConfiguration;
import io.joss.bgp.config.nodes.impl.CapabilitiesImpl;
import io.joss.bgp.config.nodes.impl.ClientConfigurationImpl;
import io.joss.bgp.config.nodes.impl.PeerConfigurationImpl;
import io.joss.bgp.net.ASType;
import io.joss.bgp.net.AddressFamily;
import io.joss.bgp.net.AddressFamilyKey;
import io.joss.bgp.net.BinaryNextHop;
import io.joss.bgp.net.NetworkLayerReachabilityInformation;
import io.joss.bgp.net.Origin;
import io.joss.bgp.net.PathSegment;
import io.joss.bgp.net.PathSegmentType;
import io.joss.bgp.net.RIBSide;
import io.joss.bgp.net.SubsequentAddressFamily;
import io.joss.bgp.net.attributes.ASPathAttribute;
import io.joss.bgp.net.attributes.IPv4MPLSLabelNLRI;
import io.joss.bgp.net.attributes.MultiProtocolReachableNLRI;
import io.joss.bgp.net.attributes.NextHopPathAttribute;
import io.joss.bgp.net.attributes.OriginPathAttribute;
import io.joss.bgp.net.attributes.PathAttribute;
import io.joss.bgp.net.capabilities.Capability;
import io.joss.bgp.net.capabilities.MultiProtocolCapability;
import io.joss.bgp.netty.fsm.FSMRegistry;
import io.joss.bgp.netty.service.BGPv4Server;
import io.joss.bgp.netty.service.RouteHandle;
import io.joss.bgp.netty.service.RouteProcessor;
import io.joss.bgp.rib.PeerRoutingInformationBase;
import io.joss.bgp.rib.PeerRoutingInformationBaseManager;
import io.joss.bgp.rib.Route;
import io.joss.bgp.rib.RouteAdded;
import io.joss.bgp.rib.RouteWithdrawn;
import io.joss.bgp.rib.RoutingEventListener;
import io.joss.bgp.rib.RoutingInformationBase;
import lombok.extern.slf4j.Slf4j;

/**
 * A higher level BGPv4 service which provides events to a registered handler.
 *
 * The BGP service itself doesn't deal with RiB handling. Instead, it passes events to the handler for the peer. A peer can have multiple
 *
 * @author theo
 *
 */

@Slf4j
public class BgpService
{

  public static AddressFamilyKey AF = AddressFamilyKey.IPV6_UNICAST_MPLS_FORWARDING;
  private final Scheduler scheduler;
  private final PeerRoutingInformationBaseManager pribm = new PeerRoutingInformationBaseManager();
  private final FSMRegistry fsmRegistry;
  private final BGPv4Server serverInstance;
  private final ApplicationConfiguration app = new ApplicationConfiguration();

  public BgpService()
  {

    try
    {
      this.scheduler = new StdSchedulerFactory().getScheduler();
    }
    catch (final SchedulerException e)
    {
      throw new RuntimeException(e);
    }

    this.app.setPeerRoutingInformationBaseManager(this.pribm);
    this.fsmRegistry = new FSMRegistry(this.app, this.scheduler);
    this.serverInstance = new BGPv4Server(this.app, this.fsmRegistry);

  }

  public void start() throws Exception
  {

    try
    {
      this.scheduler.start();
    }
    catch (final SchedulerException e)
    {
      throw new RuntimeException(e);
    }

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
   */

  public void stop()
  {

    this.fsmRegistry.stopFiniteStateMachines();

    if (this.serverInstance != null)
    {
      this.serverInstance.stopServer();
    }

    this.fsmRegistry.destroyRegistry();

  }


  public void addPeer(final InetSocketAddress addr, final int local, final int remote, final RouteProcessor proc)
  {
    
    // ---

    // ----

    final List<PathSegment> segs = new LinkedList<>();
    segs.add(new PathSegment(ASType.AS_NUMBER_2OCTETS, PathSegmentType.AS_SEQUENCE, new int[] { local }));

    final Collection<PathAttribute> pathAttributes = new LinkedList<>();
    pathAttributes.add(new ASPathAttribute(ASType.AS_NUMBER_2OCTETS, segs));
    pathAttributes.add(new NextHopPathAttribute((Inet4Address) addr.getAddress()));
    pathAttributes.add(new OriginPathAttribute(Origin.INCOMPLETE));
    
    pathAttributes.add(new MultiProtocolReachableNLRI(
        AF.getAddressFamily(),
        AF.getSubsequentAddressFamily(),
        new BinaryNextHop(new byte[] { 8, 1, 1, 1, 1, 2, 3, 4, 32 })));

    // ----

    final NetworkLayerReachabilityInformation nlri = new NetworkLayerReachabilityInformation(56, new byte[] { 0, 2, 1, 2, 3, 4, 5 });
    final Route route = new Route(
        AF,
        nlri,
        pathAttributes,
        new BinaryNextHop(addr.getAddress().getAddress()));

    // ----

    final PeerRoutingInformationBase prib = this.pribm.peerRoutingInformationBase("test");

    prib.allocateRoutingInformationBase(RIBSide.Local, AF);
    prib.allocateRoutingInformationBase(RIBSide.Remote, AF);

    final RoutingInformationBase rib = prib.routingBase(RIBSide.Local, AF);

    rib.addRoute(route);

    prib.routingBase(RIBSide.Remote, AF).addPerRibListener(new RoutingEventListener() {

      private final Map<NetworkLayerReachabilityInformation, RouteHandle> handles = new HashMap<>();

      @Override
      public void routeAdded(final RouteAdded event)
      {
        final Route r = event.getRoute();
        final IPv4MPLSLabelNLRI nlri = new IPv4MPLSLabelNLRI(r.getNlri().getPrefix());
        final RouteHandle handle = proc.add(nlri, r.getNextHop(), r.getPathAttributes());

        log.info("Route ADD[{}]: nh={}: {}", nlri.getAddress(), event.getRoute().getNextHop(), event.getRoute().getPathAttributes());

        if (handle != null)
        {
          this.handles.put(nlri.getAddress(), handle);
        }

      }

      @Override
      public void routeWithdrawn(final RouteWithdrawn event)
      {
        final Route r = event.getRoute();
        final IPv4MPLSLabelNLRI nlri = new IPv4MPLSLabelNLRI(r.getNlri().getPrefix());
        log.info("Route DEL: {}: {}", nlri.getAddress(), event.getRoute());
        final RouteHandle handle = this.handles.get(nlri.getAddress());

        if (handle != null)
        {
          handle.withdraw(r.getPathAttributes());
        }
        else
        {
          log.warn("failed to find route that is withdrawn: {}", r);
        }
      }

    });

    // ----

    try
    {

      final ClientConfigurationImpl clientConfig = new ClientConfigurationImpl(addr);

      final PeerConfigurationImpl config = new PeerConfigurationImpl(
          "test",
          clientConfig,
          local,
          remote,
          1,
          get(addr.getAddress()));

      final CapabilitiesImpl caps = new CapabilitiesImpl(new Capability[] {
          // new AutonomousSystem4Capability(16),
          new MultiProtocolCapability(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING),
          new MultiProtocolCapability(AF.getAddressFamily(), AF.getSubsequentAddressFamily()),
          // new RouteRefreshCapability()
      });

      config.setCapabilities(caps);

      this.app.addPeer(config);

    }
    catch (final ConfigurationException ex)
    {
      log.error("Config Error", ex);
      throw new RuntimeException(ex);
    }

  }

  private static long get(final InetAddress a)
  {
    final byte[] b = a.getAddress();
    final long i = 16843266L;
    return i;
  }

}
