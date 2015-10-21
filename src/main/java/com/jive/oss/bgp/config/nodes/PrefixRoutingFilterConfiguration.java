/**
 * 
 */
package com.jive.oss.bgp.config.nodes;

import java.util.Set;

import com.jive.oss.bgp.net.NetworkLayerReachabilityInformation;

/**
 * @author rainer
 *
 */
public interface PrefixRoutingFilterConfiguration extends RoutingFilterConfiguration {

	/**
	 * get the route prefixes which are filtered out 
	 * 
	 * @return
	 */
	public Set<NetworkLayerReachabilityInformation> getFilterPrefixes();
}
