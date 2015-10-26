package com.jive.oss.bgp.netty.service;

import java.util.Set;

import com.jive.oss.bgp.net.NextHop;
import com.jive.oss.bgp.net.attributes.PathAttribute;

public interface RouteHandle
{

  void update(NextHop nh, Set<PathAttribute> attrs);
  void withdraw(Set<PathAttribute> attrs);

}
