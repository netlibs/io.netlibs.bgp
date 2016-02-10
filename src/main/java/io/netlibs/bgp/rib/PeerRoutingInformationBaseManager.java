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
 * File: org.bgp4j.rib.RoutingInformationBaseManager.java
 */
package io.netlibs.bgp.rib;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

/**
 *
 *
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

public class PeerRoutingInformationBaseManager implements ExtensionRoutingBaseManager
{

  private final Map<String, PeerRoutingInformationBase> peerRibs = Collections.synchronizedMap(new HashMap<String, PeerRoutingInformationBase>());

  // private @Inject Event<PeerRoutingInformationBaseCreated> peerRibCreated;
  // private @Inject Event<PeerRoutingInformationBaseDestroyed> peerRibDestroyed;
  // private @Inject Instance<PeerRoutingInformationBase> pribProvider;

  public PeerRoutingInformationBase peerRoutingInformationBase(final String peerName)
  {

    if (StringUtils.isBlank(peerName))
    {
      throw new IllegalArgumentException("empty peer name");
    }

    PeerRoutingInformationBase result = null;
    boolean created = false;

    synchronized (this.peerRibs)
    {
      if (!this.peerRibs.containsKey(peerName))
      {
        final PeerRoutingInformationBase prib = new PeerRoutingInformationBase();
        prib.setPeerName(peerName);
        this.peerRibs.put(peerName, prib);
        created = true;
      }
      result = this.peerRibs.get(peerName);
      System.err.println(peerName + " " + this);
    }

    if (created)
    {
      System.err.println("Created: " + peerName + " " + this);
      // this.peerRibCreated.fire(new PeerRoutingInformationBaseCreated(peerName));
    }

    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.rib.ExtensionRoutingBaseManager#extensionRoutingInformationBase(java.lang.String, java.lang.String)
   */
  @Override
  public PeerRoutingInformationBase extensionRoutingInformationBase(final String extensionName, final String key)
  {
    if (StringUtils.isBlank(extensionName))
    {
      throw new IllegalArgumentException("empty extension name");
    }

    if (StringUtils.isBlank(key))
    {
      throw new IllegalArgumentException("empty key");
    }

    final PeerRoutingInformationBase prib = this.peerRoutingInformationBase(extensionName + "_" + key);

    prib.setExtensionRoutingBase(true);

    return prib;
  }

  public void destroyPeerRoutingInformationBase(final String peerName)
  {
    PeerRoutingInformationBase prib = null;

    synchronized (this.peerRibs)
    {
      prib = this.peerRibs.remove(peerName);
    }

    if (prib != null)
    {
      // this.peerRibDestroyed.fire(new PeerRoutingInformationBaseDestroyed(prib.getPeerName()));
    }
  }

  public void vistPeerRoutingBases(final PeerRoutingInformationBaseVisitor visitor)
  {
    synchronized (this.peerRibs)
    {
      for (final Entry<String, PeerRoutingInformationBase> entry : this.peerRibs.entrySet())
      {
        entry.getValue().vistPeerRoutingBases(visitor);
      }
    }
  }

  /**
   * completely reset the manager and release all RIB instances inside
   */
  public void resetManager()
  {
    this.peerRibs.clear();
  }

  /**
   * check if a peer has created a peer routing information base
   *
   * @param peerName
   * @return <code>true</code> if a peer routing information base with the given name exists, <code>false</code> otherwise
   */
  public boolean isPeerRoutingInformationBaseAvailable(final String peerName)
  {
    if (StringUtils.isBlank(peerName))
    {
      return false;
    }

    synchronized (this.peerRibs)
    {
      return this.peerRibs.containsKey(peerName);
    }
  }

}
