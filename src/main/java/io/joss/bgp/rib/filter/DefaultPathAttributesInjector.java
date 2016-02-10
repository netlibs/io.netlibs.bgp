/**
 * 
 */
package io.joss.bgp.rib.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.joss.bgp.config.nodes.PathAttributeConfiguration;
import io.joss.bgp.net.attributes.PathAttribute;
import io.joss.bgp.rib.Route;

/**
 * @author rainer
 *
 */
public class DefaultPathAttributesInjector {

	private Map<Class<? extends PathAttribute>, PathAttribute> attrs = new HashMap<Class<? extends PathAttribute>, PathAttribute>();
	
	public void configure(PathAttributeConfiguration configuration) {
		for(PathAttribute pa : configuration.getAttributes()) {
			attrs.put(pa.getClass(), pa);
		}
	}
	
	public Route injectMissingPathAttribute(Route route) {
		Set<PathAttribute> result = new TreeSet<PathAttribute>(route.getPathAttributes());
		Map<Class<? extends PathAttribute>, PathAttribute> pathClazz = new HashMap<Class<? extends PathAttribute>, PathAttribute>();
		
		for(PathAttribute pa : result) {
			pathClazz.put(pa.getClass(), pa);
		}
		
		for(Class<? extends PathAttribute> clazz : attrs.keySet()) {
			if(!pathClazz.containsKey(clazz))
				result.add(attrs.get(clazz));
		}

		return new Route(route, null, result, null);
	}
}
