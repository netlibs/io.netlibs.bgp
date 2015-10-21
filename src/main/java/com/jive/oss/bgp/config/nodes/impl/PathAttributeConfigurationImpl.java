/**
 * 
 */
package com.jive.oss.bgp.config.nodes.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.jive.oss.bgp.config.nodes.PathAttributeConfiguration;
import com.jive.oss.bgp.net.attributes.PathAttribute;

/**
 * @author rainer
 *
 */
class PathAttributeConfigurationImpl implements PathAttributeConfiguration {

	private Set<PathAttribute> attributes = new TreeSet<PathAttribute>();

	PathAttributeConfigurationImpl() {}
	
	PathAttributeConfigurationImpl(Collection<PathAttribute> attributes) {
		if(attributes != null)
			this.attributes.addAll(attributes);
	}
	
	@Override
	public Set<PathAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	void setAttributes(Set<PathAttribute> attributes) {
		this.attributes.clear();
		
		if(attributes != null)
			this.attributes.addAll(attributes);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		
		for(PathAttribute a : attributes)
			builder.append(a);
		
		return builder.toHashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof PathAttributeConfiguration))
			return false;
		
		return (compareTo((PathAttributeConfiguration)obj) == 0);
	}
	
	@Override
	public int compareTo(PathAttributeConfiguration o) {
		CompareToBuilder builder = new CompareToBuilder();
		
		builder.append(getAttributes().size(), o.getAttributes().size());
		
		if(builder.toComparison() == 0) {
			Iterator<PathAttribute> lit = getAttributes().iterator();
			Iterator<PathAttribute> rit = o.getAttributes().iterator();
			
			while(lit.hasNext())
				builder.append(lit.next(), rit.next());
		}
		
		return builder.toComparison();
	}

}
