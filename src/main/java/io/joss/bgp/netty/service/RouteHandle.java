package io.joss.bgp.netty.service;

import java.util.Set;

import io.joss.bgp.net.NextHop;
import io.joss.bgp.net.attributes.PathAttribute;

public interface RouteHandle
{

  void update(NextHop nh, Set<PathAttribute> attrs);
  void withdraw(Set<PathAttribute> attrs);

}
