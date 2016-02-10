/**
 * 
 */
package io.joss.bgp.rib.processor;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.joss.bgp.config.nodes.PathAttributeConfiguration;
import io.joss.bgp.config.nodes.PrefixRoutingFilterConfiguration;
import io.joss.bgp.config.nodes.RoutingFilterConfiguration;
import io.joss.bgp.rib.RouteAdded;
import io.joss.bgp.rib.RouteWithdrawn;
import io.joss.bgp.rib.RoutingEventListener;
import io.joss.bgp.rib.RoutingInformationBase;
import io.joss.bgp.rib.filter.DefaultPathAttributesInjector;
import io.joss.bgp.rib.filter.PrefixRoutingFilter;
import io.joss.bgp.rib.filter.RoutingFilter;
import lombok.RequiredArgsConstructor;

/**
 * @author rainer
 *
 */

@RequiredArgsConstructor
public class RouteTransportListener implements RoutingEventListener
{

  private RoutingInformationBase target;
  private RoutingInformationBase source;
  private final DefaultPathAttributesInjector injector;
  private final PrefixRoutingFilter prefixFilterProvider;
  private List<RoutingFilter> filters = new LinkedList<RoutingFilter>();

  /*
   * (non-Javadoc)
   * 
   * @see org.bgp4j.rib.RoutingEventListener#routeAdded(org.bgp4j.rib.RouteAdded)
   */
  @Override
  public void routeAdded(RouteAdded event)
  {
    boolean match = false;

    for (RoutingFilter filter : filters)
    {
      match |= filter.matchFilter(event.getRoute());

      if (match)
        break;
    }

    if (!match)
      target.addRoute(injector.injectMissingPathAttribute(event.getRoute()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.bgp4j.rib.RoutingEventListener#routeWithdrawn(org.bgp4j.rib.RouteWithdrawn)
   */
  @Override
  public void routeWithdrawn(RouteWithdrawn event)
  {
    boolean match = false;

    for (RoutingFilter filter : filters)
    {
      match |= filter.matchFilter(event.getRoute());

      if (match)
        break;
    }

    if (!match)
      target.withdrawRoute(event.getRoute());
  }

  /**
   * @param target
   *          the target to set
   */
  void setTarget(RoutingInformationBase target)
  {
    this.target = target;
  }

  /**
   * @return the source
   */
  RoutingInformationBase getSource()
  {
    return source;
  }

  /**
   * @param source
   *          the source to set
   */
  void setSource(RoutingInformationBase source)
  {
    this.source = source;
  }

  public void configure(Set<RoutingFilterConfiguration> localRoutingFilters, PathAttributeConfiguration localDefaultPathAttributes)
  {
    injector.configure(localDefaultPathAttributes);

    for (RoutingFilterConfiguration filterConfig : localRoutingFilters)
    {
      if (filterConfig instanceof PrefixRoutingFilterConfiguration)
      {
        PrefixRoutingFilter filter = prefixFilterProvider;

        filter.configure((PrefixRoutingFilterConfiguration) filterConfig);
        filters.add(filter);
      }
    }
  }

}
