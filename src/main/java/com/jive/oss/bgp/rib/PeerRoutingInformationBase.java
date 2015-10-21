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
package com.jive.oss.bgp.rib;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.jive.oss.bgp.net.AddressFamilyKey;
import com.jive.oss.bgp.net.RIBSide;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class PeerRoutingInformationBase
{

  private String peerName;
  private boolean extensionRoutingBase;

  private final Map<AddressFamilyKey, RoutingInformationBase> localRIBs = new HashMap<AddressFamilyKey, RoutingInformationBase>();
  private final Map<AddressFamilyKey, RoutingInformationBase> remoteRIBs = new HashMap<AddressFamilyKey, RoutingInformationBase>();

  // private @Inject Instance<RoutingInformationBase> ribProvider;
  // private @Inject Event<RoutingInformationBaseCreated> created;
  // private @Inject Event<RoutingInformationBaseDestroyed> destroyed;

  private final List<RoutingEventListener> listeners = Collections.synchronizedList(new LinkedList<RoutingEventListener>());

  public PeerRoutingInformationBase()
  {
  }

  /**
   * @return the peerName
   */
  public String getPeerName()
  {
    return this.peerName;
  }

  public void allocateRoutingInformationBase(final RIBSide side, final AddressFamilyKey afk)
  {
    RoutingInformationBase rib = null;

    switch (side)
    {
      case Local:
        rib = this.allocateRoutingInformationBase(this.localRIBs, side, afk);
        break;
      case Remote:
        rib = this.allocateRoutingInformationBase(this.remoteRIBs, side, afk);
        break;
    }

    if (rib != null)
    {
      // this.created.fire(new RoutingInformationBaseCreated(this.peerName, side, afk));
      rib.setListeners(this.listeners);
    }
  }

  public void destroyRoutingInformationBase(final RIBSide side, final AddressFamilyKey afk)
  {
    boolean found = false;

    switch (side)
    {
      case Local:
        found = this.destroyRoutingInformationBase(this.localRIBs, afk);
        break;
      case Remote:
        found = this.destroyRoutingInformationBase(this.remoteRIBs, afk);
        break;
    }

    if (found)
    {
      // this.destroyed.fire(new RoutingInformationBaseDestroyed(this.peerName, side, afk));
    }

  }

  public RoutingInformationBase routingBase(final RIBSide side, final AddressFamilyKey afk)
  {
    RoutingInformationBase rib = null;

    switch (side)
    {
      case Local:
        rib = this.localRIBs.get(afk);
        break;
      case Remote:
        rib = this.remoteRIBs.get(afk);
        break;
    }

    return rib;
  }

  public void visitRoutingBases(final RIBSide side, final RoutingInformationBaseVisitor visitor, final Set<AddressFamilyKey> wanted)
  {
    switch (side)
    {
      case Local:
        this.visitRoutingBases(this.localRIBs, visitor, wanted);
        break;
      case Remote:
        this.visitRoutingBases(this.remoteRIBs, visitor, wanted);
        break;
    }

  }

  public void visitRoutingBases(final RIBSide side, final RoutingInformationBaseVisitor visitor)
  {
    this.visitRoutingBases(side, visitor, null);
  }

  public void vistPeerRoutingBases(final PeerRoutingInformationBaseVisitor visitor)
  {
    for (final Entry<AddressFamilyKey, RoutingInformationBase> entry : this.localRIBs.entrySet())
    {
      visitor.visitRoutingBase(this.peerName, entry.getValue().getRibID(), entry.getKey(), RIBSide.Local);
    }
    for (final Entry<AddressFamilyKey, RoutingInformationBase> entry : this.remoteRIBs.entrySet())
    {
      visitor.visitRoutingBase(this.peerName, entry.getValue().getRibID(), entry.getKey(), RIBSide.Remote);
    }
  }

  private void visitRoutingBases(final Map<AddressFamilyKey, RoutingInformationBase> ribs, final RoutingInformationBaseVisitor visitor, Set<AddressFamilyKey> wanted)
  {
    if (wanted == null)
    {
      wanted = ribs.keySet();
    }

    for (final Entry<AddressFamilyKey, RoutingInformationBase> ribEntry : ribs.entrySet())
    {
      if (wanted.contains(ribEntry.getKey()))
      {
        ribEntry.getValue().visitRoutingNodes(visitor);
      }
    }
  }

  private RoutingInformationBase allocateRoutingInformationBase(final Map<AddressFamilyKey, RoutingInformationBase> ribs, final RIBSide side, final AddressFamilyKey afk)
  {
    RoutingInformationBase rib = null;

    if (!ribs.containsKey(afk))
    {
      rib = new RoutingInformationBase();
      rib.setAddressFamilyKey(afk);
      rib.setPeerName(this.peerName);
      rib.setSide(side);
      ribs.put(afk, rib);
    }

    return rib;
  }

  private boolean destroyRoutingInformationBase(final Map<AddressFamilyKey, RoutingInformationBase> ribs, final AddressFamilyKey afk)
  {
    boolean found = false;

    if ((found = ribs.containsKey(afk)))
    {
      ribs.remove(afk).destroyRIB();
    }

    return found;
  }

  /**
   * @param peerName
   *          the peerName to set
   */
  void setPeerName(final String peerName)
  {
    this.peerName = peerName;
  }

  public void destroyAllRoutingInformationBases()
  {
    for (final Entry<AddressFamilyKey, RoutingInformationBase> entry : this.localRIBs.entrySet())
    {
      entry.getValue().destroyRIB();
    }
    for (final Entry<AddressFamilyKey, RoutingInformationBase> entry : this.remoteRIBs.entrySet())
    {
      entry.getValue().destroyRIB();
    }

    this.localRIBs.clear();
    this.remoteRIBs.clear();
  }

  public void addRoutingListener(final RoutingEventListener listener)
  {
    this.listeners.add(listener);
  }

  public void removeRoutingListener(final RoutingEventListener listener)
  {
    this.listeners.remove(listener);
  }

  /**
   * @return the extensionRoutingBase
   */
  public boolean isExtensionRoutingBase()
  {
    return this.extensionRoutingBase;
  }

  /**
   * @param extensionRoutingBase
   *          the extensionRoutingBase to set
   */
  void setExtensionRoutingBase(final boolean extensionRoutingBase)
  {
    this.extensionRoutingBase = extensionRoutingBase;
  }
}
