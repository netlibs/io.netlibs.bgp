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
 * File: org.bgp4j.rib.RoutingTree.java
 */
package io.joss.bgp.rib;

import java.util.NavigableSet;
import java.util.TreeSet;

import io.joss.bgp.net.NetworkLayerReachabilityInformation;

/**
 * This class builds and manages a tree of (NLRI, Path attributes) tuples. The tree is build top-down whereas the parent node always
 * contains more coarse-grained routing information than the child nodes.
 *
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

class RoutingTree
{

  /**
   * Internal node of the routing tree. The discriminating fact is the NLRI attached to the node. Therefore the ordering of the tree nodes
   * is based solely on NLRI ordering.
   *
   * @author Rainer Bieniek (Rainer.Bieniek@web.de)
   *
   */

  class RoutingTreeNode implements Comparable<RoutingTreeNode>
  {
    private final Route route;
    private final NavigableSet<RoutingTreeNode> childNodes = new TreeSet<RoutingTree.RoutingTreeNode>();

    public RoutingTreeNode(final Route route)
    {
      this.route = route;
    }

    @Override
    public int compareTo(final RoutingTreeNode o)
    {
      if (this.route.getNlri() == null)
      {
        if (o.getRoute().getNlri() == null)
        {
          return 0;
        }
        return o.getRoute().getNlri().compareTo(null);
      }
      return this.route.getNlri().compareTo(o.getRoute().getNlri());
    }

    @Override
    public int hashCode()
    {
      return this.route.getNlri().hashCode();
    }

    @Override
    public boolean equals(final Object o)
    {
      if (!(o instanceof RoutingTreeNode))
      {
        return false;
      }

      return this.route.getNlri().equals(((RoutingTreeNode) o).getRoute().getNlri());
    }

    /**
     * @return the childNodes
     */
    NavigableSet<RoutingTreeNode> getChildNodes()
    {
      return this.childNodes;
    }

    /**
     * @return the route
     */
    public Route getRoute()
    {
      return this.route;
    }
  }

  // the root of all nodes managed by this routing tree. This is the only node w/o a (NLRI prefix, Path attributes) tuple attached to it
  private final RoutingTreeNode rootNode = new RoutingTreeNode(null);

  /**
   * Destroy the routing tree and delete all information held within.
   */
  void destroy()
  {
    this.rootNode.getChildNodes().clear();
  }

  /**
   * Add a (NLRI, Path attributes) tuple to the tree
   *
   * @param nlri
   *          the NLRI prefix to be added
   * @param pathAttributes
   *          the path attributes belonging to this prefix
   * @return <code>true<code> if the node was added, <code>false</code> if the node was not added
   */
  synchronized boolean addRoute(final Route route)
  {
    return this.addRoute(this.rootNode, new RoutingTreeNode(route));
  }

  /**
   * Add a new node to a parent routing tree node. The rules for this process are as follows:
   * <ol>
   * <li>If a child node NLRI prefix is less specific than the NLRI prefix of the new node, recursively descend with the child node as the
   * new parent node</li>
   * <li>If the new node NLRI prefix is less specific than one or more child node NLRI prefixes, add the new node and reparent the child
   * nodes to the new node</li>
   * <li>If a child node NLRI prefix equals the new node NLRI prefix, then the path attributes are replaced</li>
   * <li>If neither of the conditions above holds true then simply add the new node to the parent.</li>
   * </ol>
   *
   * @param parent
   * @param newNode
   * @return
   */
  private boolean addRoute(final RoutingTreeNode parent, final RoutingTreeNode newNode)
  {
    boolean added = false;
    boolean handled = false;
    final NavigableSet<RoutingTreeNode> reparentedNodes = new TreeSet<RoutingTree.RoutingTreeNode>();

    for (final RoutingTreeNode child : parent.getChildNodes())
    {
      if (child.getRoute().getNlri().equals(newNode.getRoute().getNlri()))
      {
        // we have an exact match on the NLRI preifxes --> just replace the path attributes but signal as addition
        child.getRoute().getPathAttributes().clear();
        child.getRoute().getPathAttributes().addAll(newNode.getRoute().getPathAttributes());

        handled = true;
        added = true;
        break;
      }
      else if (NlriComparator.isPrefixOf(child.getRoute(), newNode.getRoute()))
      {
        // a child node has more coarse-grained routing info attached --> make this child node parent of the new node
        added = this.addRoute(child, newNode);
        handled = true;
        break;
      }
      else if (NlriComparator.isPrefixOf(newNode.getRoute(), child.getRoute()))
      {
        // the new node has more coarse-grained routing info attached --> the child must be reparented to the new node.
        reparentedNodes.add(child);
      }
    }

    if (!handled)
    {
      parent.getChildNodes().add(newNode);
      added = true;

      // we have nodes that need to be reparented to the new node
      if (reparentedNodes.size() > 0)
      {
        newNode.getChildNodes().addAll(reparentedNodes);
        parent.getChildNodes().removeAll(reparentedNodes);
      }
    }

    return added;
  }

  /**
   * Withdraw the (NLRI, Path attributes) tuple from the tree.
   *
   * @param nlri
   *          the NLRI prefix to withdraw
   * @return <code>true</code> if the node was removed, <code>false</code> otherwise
   */

  synchronized boolean withdrawRoute(final Route route)
  {
    return this.withdrawRoute(this.rootNode, route);
  }

  /**
   * recursively descend into the tree
   *
   * @param visitor
   */
  synchronized void visitTree(final RoutingTreeVisitor visitor)
  {
    this.visitTree(this.rootNode, visitor);
  }

  public NlriComparator comparator(final int type)
  {
    return null;
  }

  /**
   * Withdraw the (NLRI prefix, Path attributes) tuple from the tree. The rules for this pürocess are as follows:
   * <ol>
   * <li>If the NLRI prefix of a child node matches the to be remove NLRI prefix, the child node is removed and all child nodes of the child
   * node are added to the parent node.</li>
   * <li>If a child node NLRI is a less specific prefix of the to be removed NLRI prefix, recursively descend into the tree rooted by the
   * child node</li>
   * </ol>
   *
   * @param parent
   * @param nlri
   * @return
   */

  private boolean withdrawRoute(final RoutingTreeNode parent, final Route route)
  {

    boolean withdrawn = false;
    RoutingTreeNode candidate = null;

    for (final RoutingTreeNode child : parent.getChildNodes())
    {
      if (NlriComparator.equals(child.getRoute(), route))
      {
        candidate = child;
        break;
      }
      else if (NlriComparator.isPrefixOf(child.getRoute(), route))
      {
        withdrawn = this.withdrawRoute(child, route);
        break;
      }

    }

    if (candidate != null)
    {
      parent.getChildNodes().addAll(candidate.getChildNodes());
      parent.getChildNodes().remove(candidate);
      withdrawn = true;
    }

    return withdrawn;
  }

  /**
   * @return the rootNode
   */
  RoutingTreeNode getRootNode()
  {
    return this.rootNode;
  }

  /**
   *
   * @param nlri
   * @return
   */

  LookupResult lookupRoute(final NetworkLayerReachabilityInformation nlri)
  {
    return this.lookupRoute(this.rootNode, nlri);
  }

  /**
   *
   */

  private LookupResult lookupRoute(final RoutingTreeNode parent, final NetworkLayerReachabilityInformation nlri)
  {

    LookupResult result = null;

    for (final RoutingTreeNode child : parent.getChildNodes())
    {

      if (NlriComparator.equals(child.getRoute(), nlri))
      {
        result = new LookupResult(child.getRoute());
        break;
      }
      else if (NlriComparator.isPrefixOf(child.getRoute(), nlri))
      {

        // child node NLRI is less specific match --> descend into child node
        result = this.lookupRoute(child, nlri);

        // child node lookup did not yield result --> build result from less specific child node NLRI
        if (result == null)
        {
          result = new LookupResult(child.getRoute());
        }

      }

    }

    return result;
  }

  /**
   * recursively descend into the tree
   *
   * @param visitor
   */
  synchronized void visitTree(final RoutingTreeNode parent, final RoutingTreeVisitor visitor)
  {
    for (final RoutingTreeNode child : parent.getChildNodes())
    {
      visitor.visitRouteTreeNode(child.getRoute());

      this.visitTree(child, visitor);
    }
  }
}
