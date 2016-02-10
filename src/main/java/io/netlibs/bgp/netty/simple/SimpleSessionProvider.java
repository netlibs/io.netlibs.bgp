package io.netlibs.bgp.netty.simple;

import io.netlibs.bgp.netty.protocol.open.OpenPacket;
import io.netlibs.ipaddr.IPv4Address;

public interface SimpleSessionProvider
{

  SimpleSessionListener allocate(IPv4Address addr, OpenPacket e);

}
