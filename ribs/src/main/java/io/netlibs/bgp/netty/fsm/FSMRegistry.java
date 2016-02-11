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
 * File: org.bgp4j.netty.fsm.FSMRegistry.java
 */
package io.netlibs.bgp.netty.fsm;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.quartz.Scheduler;

import io.netlibs.bgp.config.global.ApplicationConfiguration;
import io.netlibs.bgp.config.global.PeerConfigurationEvent;
import io.netlibs.bgp.config.nodes.PeerConfiguration;
import io.netlibs.bgp.netty.service.BGPv4Client;
import io.netlibs.bgp.rib.PeerRoutingInformationBaseManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@Slf4j
public class FSMRegistry
{

  private final ApplicationConfiguration applicationConfiguration;

  private Scheduler scheduler;

  public FSMRegistry(ApplicationConfiguration config, Scheduler scheduler)
  {
    this.applicationConfiguration = config;
    this.scheduler = scheduler;
  }

  private final Map<InetSocketAddress, BGPv4FSM> fsmMap = new HashMap<InetSocketAddress, BGPv4FSM>();

  // private @Inject ApplicationConfiguration applicationConfiguration;
  private boolean haveRunningMachines = false;

  public void createRegistry()
  {
    for (final PeerConfiguration peerConfig : this.applicationConfiguration.listPeerConfigurations())
    {
      try
      {
        final BGPv4FSM fsm = createFsm();
        fsm.configure(peerConfig);
        this.fsmMap.put(fsm.getRemotePeerAddress(), fsm);
      }
      catch (final Exception e)
      {
        FSMRegistry.log.error("Internal error: cannot create peer " + peerConfig.getPeerName(), e);
      }
    }
  }

  private BGPv4FSM createFsm()
  {
    return new BGPv4FSM(scheduler, new BGPv4Client(this), new CapabilitesNegotiator(), applicationConfiguration.getPeerRoutingInformationBaseManager(), new OutboundRoutingUpdateQueue(scheduler));
  }

  public void registerFSM(final BGPv4FSM fsm)
  {
    synchronized (this.fsmMap)
    {
      this.fsmMap.put(fsm.getRemotePeerAddress(), fsm);
    }
  }

  public BGPv4FSM lookupFSM(final InetSocketAddress peerAddress)
  {
    synchronized (this.fsmMap)
    {
      return this.fsmMap.get(peerAddress);
    }
  }

  public BGPv4FSM lookupFSM(final InetAddress peerAddress)
  {
    final List<BGPv4FSM> candidates = new LinkedList<BGPv4FSM>();
    BGPv4FSM fsm = null;

    synchronized (this.fsmMap)
    {
      for (final Entry<InetSocketAddress, BGPv4FSM> fsmEntry : this.fsmMap.entrySet())
      {
        if (fsmEntry.getKey().getAddress().equals(peerAddress))
        {
          candidates.add(fsmEntry.getValue());
        }
      }
    }

    if (candidates.size() > 1)
    {
      throw new IllegalStateException("Having more than one FSM instance for address " + peerAddress);
    }
    else if (candidates.size() == 1)
    {
      fsm = candidates.get(0);
    }

    return fsm;
  }

  public void destroyRegistry()
  {
    for (final InetSocketAddress addr : this.fsmMap.keySet())
    {
      this.fsmMap.get(addr).destroyFSM();
    }

    this.fsmMap.clear();
  }

  public void peerChanged(final PeerConfigurationEvent event)
  {

    BGPv4FSM fsm = null;
    InetSocketAddress remotePeerAddress = null;

    switch (event.getType())
    {
      case CONFIGURATION_ADDED:
        try
        {

          fsm = this.createFsm();

          fsm.configure(event.getCurrent());

          synchronized (this.fsmMap)
          {
            this.fsmMap.put(fsm.getRemotePeerAddress(), fsm);
          }

          if (this.haveRunningMachines)
          {
            fsm.startFSMAutomatic();
          }
        }
        catch (final Exception e)
        {
          FSMRegistry.log.error("Internal error: cannot create peer " + event.getCurrent().getPeerName());
        }

        break;
      case CONFIGURATION_REMOVED:
        remotePeerAddress = event.getFormer().getClientConfig().getRemoteAddress();

        synchronized (this.fsmMap)
        {
          fsm = this.fsmMap.remove(remotePeerAddress);
        }

        if (fsm != null)
        {
          fsm.stopFSM();
          fsm.destroyFSM();
        }
        break;
    }
  }

  public void startFiniteStateMachines()
  {
    for (final Entry<InetSocketAddress, BGPv4FSM> entry : this.fsmMap.entrySet())
    {
      FSMRegistry.log.info("starting FSM automatic for connection to " + entry.getKey());

      entry.getValue().startFSMAutomatic();
    }
    this.haveRunningMachines = true;
  }

  public void stopFiniteStateMachines()
  {
    this.haveRunningMachines = false;

    for (final Entry<InetSocketAddress, BGPv4FSM> entry : this.fsmMap.entrySet())
    {
      FSMRegistry.log.info("stopping FSM automatic for connection to " + entry.getKey());

      entry.getValue().stopFSM();
    }
  }
}
