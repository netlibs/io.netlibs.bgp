/**
 * 
 */
package com.jive.oss.bgp.config.nodes;

import java.util.Set;

/**
 * @author rainer
 *
 */
public interface RoutingProcessorConfiguration extends Comparable<RoutingProcessorConfiguration> {
	
	public Set<RoutingInstanceConfiguration> getRoutingInstances();
}
