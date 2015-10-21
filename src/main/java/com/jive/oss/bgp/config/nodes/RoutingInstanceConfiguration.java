/**
 * 
 */
package com.jive.oss.bgp.config.nodes;

/**
 * @author rainer
 *
 */
public interface RoutingInstanceConfiguration extends Comparable<RoutingInstanceConfiguration> {

	public RoutingPeerConfiguration getFirstPeer();
	
	public RoutingPeerConfiguration getSecondPeer();
}
