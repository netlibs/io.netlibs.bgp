package com.jive.oss.bgp.netty.simple;

import com.jive.oss.bgp.netty.protocol.open.OpenPacket;
import com.jive.oss.commons.ip.IPv4Address;

public interface SimpleSessionProvider
{

  SimpleSessionListener allocate(IPv4Address addr, OpenPacket e);

}
