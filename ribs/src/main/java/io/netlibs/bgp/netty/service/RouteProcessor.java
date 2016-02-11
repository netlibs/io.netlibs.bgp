package io.netlibs.bgp.netty.service;

import java.util.Set;

import io.netlibs.bgp.protocol.NextHop;
import io.netlibs.bgp.protocol.attributes.IPv4MPLSLabelNLRI;
import io.netlibs.bgp.protocol.attributes.PathAttribute;

/**
 * Handler for dealing with rout peer updates.
 */

public interface RouteProcessor
{

  public RouteHandle add(final IPv4MPLSLabelNLRI nlri, final NextHop nh, final Set<PathAttribute> attrs);

}
