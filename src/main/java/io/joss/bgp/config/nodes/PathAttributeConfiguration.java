/**
 * 
 */
package io.joss.bgp.config.nodes;

import java.util.Set;

import io.joss.bgp.net.attributes.PathAttribute;

/**
 * @author rainer
 *
 */
public interface PathAttributeConfiguration extends Comparable<PathAttributeConfiguration>{

	/**
	 * 
	 * @return
	 */
	public Set<PathAttribute> getAttributes();
}
