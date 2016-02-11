package io.netlibs.bgp.netty.simple;

import io.netlibs.bgp.netty.protocol.open.OpenPacket;
import io.netlibs.ipaddr.IPv4Address;

@FunctionalInterface
public interface BGPv4SessionFactory
{

  RemoteConfig allocate(IPv4Address addr, OpenPacket e);

}
