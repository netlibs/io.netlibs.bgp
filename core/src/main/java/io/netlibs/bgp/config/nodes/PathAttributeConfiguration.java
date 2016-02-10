/**
 * 
 */
package io.netlibs.bgp.config.nodes;

import java.util.Set;

import io.netlibs.bgp.net.attributes.PathAttribute;

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
