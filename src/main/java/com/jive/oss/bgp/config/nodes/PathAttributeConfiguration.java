/**
 * 
 */
package com.jive.oss.bgp.config.nodes;

import java.util.Set;

import com.jive.oss.bgp.net.attributes.PathAttribute;

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
