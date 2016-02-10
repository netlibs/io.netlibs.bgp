package io.joss.bgp.netty.simple;

import com.jive.oss.commons.ip.IPv4Address;

import io.joss.bgp.netty.protocol.open.OpenPacket;

public interface SimpleSessionProvider
{

  SimpleSessionListener allocate(IPv4Address addr, OpenPacket e);

}
