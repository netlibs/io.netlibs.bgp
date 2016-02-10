package io.joss.bgp.netty.service;

import java.util.Set;

import io.joss.bgp.net.NextHop;
import io.joss.bgp.net.attributes.IPv4MPLSLabelNLRI;
import io.joss.bgp.net.attributes.PathAttribute;

/**
 * Handler for dealing with rout peer updates.
 */

public interface RouteProcessor
{

  public RouteHandle add(final IPv4MPLSLabelNLRI nlri, final NextHop nh, final Set<PathAttribute> attrs);

}
