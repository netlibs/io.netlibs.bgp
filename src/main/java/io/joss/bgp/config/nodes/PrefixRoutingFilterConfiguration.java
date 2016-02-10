/**
 * 
 */
package io.joss.bgp.config.nodes;

import java.util.Set;

import io.joss.bgp.net.NetworkLayerReachabilityInformation;

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
