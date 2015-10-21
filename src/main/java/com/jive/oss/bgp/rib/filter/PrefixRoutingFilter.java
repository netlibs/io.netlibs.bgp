/**
 * 
 */
package com.jive.oss.bgp.rib.filter;

import java.util.Set;
import java.util.TreeSet;

import com.jive.oss.bgp.config.nodes.PrefixRoutingFilterConfiguration;
import com.jive.oss.bgp.net.NetworkLayerReachabilityInformation;
import com.jive.oss.bgp.rib.Route;

/**
 * @author rainer
 *
 */
public class PrefixRoutingFilter implements RoutingFilter {

	private Set<NetworkLayerReachabilityInformation> filterPrefixes = new TreeSet<NetworkLayerReachabilityInformation>();

	public void configure(PrefixRoutingFilterConfiguration configuration) {
		if(configuration != null)
			filterPrefixes.addAll(configuration.getFilterPrefixes());
	}
	
	/* (non-Javadoc)
	 * @see org.bgp4j.rib.filter.RoutingFilter#matchFilter(org.bgp4j.net.NetworkLayerReachabilityInformation, org.bgp4j.net.NextHop, java.util.Set)
	 */
	@Override
	public boolean matchFilter(Route route) {
		boolean match = false;
		
		for(NetworkLayerReachabilityInformation filterPrefix : filterPrefixes) {
			if(filterPrefix.isPrefixOf(route.getNlri()) || filterPrefix.equals(route.getNlri())) {
				match = true;
				break;
			}
		}
		
		return match;
	}

}
