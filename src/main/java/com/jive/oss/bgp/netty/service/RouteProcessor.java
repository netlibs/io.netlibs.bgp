package com.jive.oss.bgp.netty.service;

import java.util.Set;

import com.jive.oss.bgp.net.NextHop;
import com.jive.oss.bgp.net.attributes.PathAttribute;

/**
 * Handler for dealing with rout peer updates.
 */

public interface RouteProcessor
{

  public RouteHandle add(final IPv4MPLSLabelNLRI nlri, final NextHop nh, final Set<PathAttribute> attrs);

}
