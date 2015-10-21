/**
 * 
 */
package com.jive.oss.bgp.rib.filter;

import com.jive.oss.bgp.rib.Route;

/**
 * @author rainer
 *
 */
public interface RoutingFilter {

	public boolean matchFilter(Route route);
}
