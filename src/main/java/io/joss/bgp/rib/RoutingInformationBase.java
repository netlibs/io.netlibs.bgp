/**
 *  Copyright 2012 Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * File: org.bgp4j.rib.RoutingInformationBase.java
 */
package io.joss.bgp.rib;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import io.joss.bgp.net.AddressFamilyKey;
import io.joss.bgp.net.NetworkLayerReachabilityInformation;
import io.joss.bgp.net.NextHop;
import io.joss.bgp.net.RIBSide;
import io.joss.bgp.net.attributes.PathAttribute;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@Slf4j
public class RoutingInformationBase
{

  private String peerName;
  private RIBSide side;
  private AddressFamilyKey addressFamilyKey;
  private final RoutingTree routingTree = new RoutingTree();
  // private @Inject Event<RouteAdded> routeAddedEvent;
  // private @Inject Event<RouteWithdrawn> routeWithdrawnEvent;
  private Collection<RoutingEventListener> listeners;
  private final List<RoutingEventListener> perRibListeners = Collections.synchronizedList(new LinkedList<RoutingEventListener>());
  private final UUID ribID = UUID.randomUUID();

  RoutingInformationBase()
  {
  }

  /**
   * @return the peerName
   */
  public String getPeerName()
  {
    return this.peerName;
  }

  /**
   * @return the side
   */
  public RIBSide getSide()
  {
    return this.side;
  }

  /**
   * @return the addressFamilyKey
   */
  public AddressFamilyKey getAddressFamilyKey()
  {
    return this.addressFamilyKey;
  }

  /**
   * @param peerName
   *          the peerName to set
   */
  void setPeerName(final String peerName)
  {
    this.peerName = peerName;
  }

  /**
   * @param side
   *          the side to set
   */
  void setSide(final RIBSide side)
  {
    this.side = side;
  }

  /**
   * @param addressFamilyKey
   *          the addressFamilyKey to set
   */
  void setAddressFamilyKey(final AddressFamilyKey addressFamilyKey)
  {
    this.addressFamilyKey = addressFamilyKey;
  }

  void destroyRIB()
  {
    this.routingTree.destroy();
  }

  /**
   * Add a NLRI collection sharing a common collection of path attributes to the routing tree
   *
   * @param nlris
   * @param pathAttributes
   */

  public void addRoutes(final Collection<NetworkLayerReachabilityInformation> nlris, final Collection<PathAttribute> pathAttributes, final NextHop nextHop)
  {

    for (final NetworkLayerReachabilityInformation nlri : nlris)
    {

      final Route route = new Route(this.getRibID(), this.getAddressFamilyKey(), nlri, pathAttributes, nextHop);

      if (this.routingTree.addRoute(route))
      {
        final RouteAdded event = new RouteAdded(this.getPeerName(), this.getSide(), route);

        // System.err.println(event);
        // this.routeAddedEvent.fire(event);

        if (this.listeners != null)
        {
          for (final RoutingEventListener listener : this.listeners)
          {
            listener.routeAdded(event);
          }
        }
        for (final RoutingEventListener listener : this.perRibListeners)
        {
          listener.routeAdded(event);
        }
      }
    }
  }

  /**
   * Withdraw a NLRI collection from the routing tree
   *
   * @param nlris
   * @param pathAttributes
   */
  public void withdrawRoutes(final Collection<NetworkLayerReachabilityInformation> nlris)
  {

    for (final NetworkLayerReachabilityInformation nlri : nlris)
    {

      final Route route = new Route(this.getRibID(), this.getAddressFamilyKey(), nlri, null, null);

      log.debug("Withdrawing {}", route);

      if (this.routingTree.withdrawRoute(route))
      {

        final RouteWithdrawn event = new RouteWithdrawn(this.getPeerName(), this.getSide(), route);

        // this.routeWithdrawnEvent.fire(event);

        if (this.listeners != null)
        {
          for (final RoutingEventListener listener : this.listeners)
          {
            listener.routeWithdrawn(event);
          }
        }

        for (final RoutingEventListener listener : this.perRibListeners)
        {
          listener.routeWithdrawn(event);
        }

      }
      else
      {
        log.warn("error: received withdrawn for unknown NLRI: {}", route);
      }

    }
  }

  /**
   * Lookup a route by a NLRI prefix. The lookup process may result in a specific, less specific route or no route at all
   *
   * @param nlri
   *          prefix to look up
   * @return the result or <code>null</code> if no result can be found.
   */

  public LookupResult lookupRoute(final NetworkLayerReachabilityInformation nlri)
  {
    return this.routingTree.lookupRoute(nlri);
  }

  /**
   * Visit all nodes in the routing tree
   *
   * @param visitor
   */

  public void visitRoutingNodes(final RoutingInformationBaseVisitor visitor)
  {
    this.routingTree.visitTree(route -> visitor.visitRouteNode(RoutingInformationBase.this.getPeerName(), RoutingInformationBase.this.getSide(), route));
  }

  /**
   *
   */

  public void addPerRibListener(final RoutingEventListener listener)
  {
    this.perRibListeners.add(listener);
  }

  /**
   *
   */

  public void removePerRibListener(final RoutingEventListener listener)
  {
    this.perRibListeners.remove(listener);
  }

  /**
   * @param listeners
   *          the listeners to set
   */

  void setListeners(final Collection<RoutingEventListener> listeners)
  {
    this.listeners = listeners;
  }

  /**
   * @return the ribID
   */

  public UUID getRibID()
  {
    return this.ribID;
  }

  /**
   *
   */

  public void addRoute(Route route)
  {

    if (route.getRibID() == null)
    {
      route = new Route(this.getRibID(), route.getAddressFamilyKey(), route.getNlri(), route.getPathAttributes(), route.getNextHop());
    }

    if (this.routingTree.addRoute(route))
    {

      final RouteAdded event = new RouteAdded(this.getPeerName(), this.getSide(), route);

      // this.routeAddedEvent.fire(event);

      if (this.listeners != null)
      {
        for (final RoutingEventListener listener : this.listeners)
        {
          listener.routeAdded(event);
        }
      }

      for (final RoutingEventListener listener : this.perRibListeners)
      {
        listener.routeAdded(event);
      }

    }
  }

  /**
   *
   */

  public void withdrawRoute(Route route)
  {

    if (route.getRibID() == null)
    {
      route = new Route(this.getRibID(), route.getAddressFamilyKey(), route.getNlri(), route.getPathAttributes(), route.getNextHop());
    }

    if (this.routingTree.withdrawRoute(route))
    {

      final RouteWithdrawn event = new RouteWithdrawn(this.getPeerName(), this.getSide(), route);

      // this.routeWithdrawnEvent.fire(event);

      if (this.listeners != null)
      {
        for (final RoutingEventListener listener : this.listeners)
        {
          listener.routeWithdrawn(event);
        }
      }

      for (final RoutingEventListener listener : this.perRibListeners)
      {
        listener.routeWithdrawn(event);
      }

    }

  }

}
