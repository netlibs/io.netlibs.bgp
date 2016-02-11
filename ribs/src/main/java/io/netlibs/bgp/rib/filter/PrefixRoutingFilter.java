/**
 *
 */
package io.netlibs.bgp.rib.filter;

import java.util.Set;
import java.util.TreeSet;

import io.netlibs.bgp.config.nodes.PrefixRoutingFilterConfiguration;
import io.netlibs.bgp.protocol.NetworkLayerReachabilityInformation;
import io.netlibs.bgp.rib.NlriComparator;
import io.netlibs.bgp.rib.Route;

/**
 * @author rainer
 *
 */

public class PrefixRoutingFilter implements RoutingFilter
{

  private final Set<NetworkLayerReachabilityInformation> filterPrefixes = new TreeSet<NetworkLayerReachabilityInformation>();

  public void configure(final PrefixRoutingFilterConfiguration configuration)
  {

    if (configuration != null)
    {
      this.filterPrefixes.addAll(configuration.getFilterPrefixes());
    }

  }

  @Override
  public boolean matchFilter(final Route route)
  {

    boolean match = false;

    for (final NetworkLayerReachabilityInformation filterPrefix : this.filterPrefixes)
    {

      if (NlriComparator.isPrefixOf(route.getAddressFamilyKey(), filterPrefix, route.getNlri())
          || NlriComparator.equals(route.getAddressFamilyKey(), filterPrefix, route.getNlri()))
      {
        match = true;
        break;
      }

    }

    return match;

  }

}
