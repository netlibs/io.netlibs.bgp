/**
 * 
 */
package io.joss.bgp.rib.filter;

import io.joss.bgp.rib.Route;

/**
 * @author rainer
 *
 */
public interface RoutingFilter {

	public boolean matchFilter(Route route);
}
