/**
 * 
 */
package com.jive.oss.bgp.config.nodes;

import java.util.Set;

import com.jive.oss.bgp.net.AddressFamilyKey;

/**
 * @author rainer
 *
 */
public interface AddressFamilyRoutingConfiguration extends Comparable<AddressFamilyRoutingConfiguration> {

	/**
	 * get the keying address / subsequent address family pair
	 * 
	 * @return
	 */
	public AddressFamilyKey getKey();
	
	/**
	 * get the routes advertised for this address / subsequent address pair
	 * 
	 * @return
	 */
	public Set<RouteConfiguration> getRoutes();
}
