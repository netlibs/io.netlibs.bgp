package io.netlibs.bgp.netty.simple;

import io.netlibs.bgp.netty.protocol.open.OpenPacket;
import io.netlibs.ipaddr.IPv4Address;

/**
 * Provides the BGP transport handler with a session handle.
 */

@FunctionalInterface
public interface BGPv4SessionFactory
{

  /**
   * Called when we see an OPEN packet from the remote side.
   * 
   * The returned {@link RemoteConfig} must contain a {@link LocalConfig} (which will generate our OPEN), and a {@link BGPv4SessionListener}
   * which will receive all BGPv4 packets and events.
   * 
   * @param the
   *          remote address
   * @param the
   *          remote OPEN
   * 
   * @return the {@link RemoteConfig} to use, or null to reject the connection.
   * 
   */

  RemoteConfig allocate(IPv4Address addr, OpenPacket remoteOpen);

}
