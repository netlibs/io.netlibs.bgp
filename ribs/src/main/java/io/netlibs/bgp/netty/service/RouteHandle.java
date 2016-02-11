package io.netlibs.bgp.netty.service;

import java.util.Set;

import io.netlibs.bgp.protocol.NextHop;
import io.netlibs.bgp.protocol.attributes.PathAttribute;

public interface RouteHandle
{

  void update(NextHop nh, Set<PathAttribute> attrs);
  void withdraw(Set<PathAttribute> attrs);

}
