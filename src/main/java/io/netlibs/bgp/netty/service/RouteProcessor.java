package io.netlibs.bgp.netty.service;

import java.util.Set;

import io.netlibs.bgp.net.NextHop;
import io.netlibs.bgp.net.attributes.IPv4MPLSLabelNLRI;
import io.netlibs.bgp.net.attributes.PathAttribute;

/**
 * Handler for dealing with rout peer updates.
 */

public interface RouteProcessor
{

  public RouteHandle add(final IPv4MPLSLabelNLRI nlri, final NextHop nh, final Set<PathAttribute> attrs);

}
