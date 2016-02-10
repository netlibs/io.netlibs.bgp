/**
 * 
 */
package io.netlibs.bgp.rib.filter;

import io.netlibs.bgp.rib.Route;

/**
 * @author rainer
 *
 */
public interface RoutingFilter {

	public boolean matchFilter(Route route);
}
