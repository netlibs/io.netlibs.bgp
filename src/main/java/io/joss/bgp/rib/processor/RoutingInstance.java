/**
 *
 */
package io.joss.bgp.rib.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import io.joss.bgp.config.nodes.AddressFamilyRoutingPeerConfiguration;
import io.joss.bgp.config.nodes.RoutingInstanceConfiguration;
import io.joss.bgp.net.AddressFamilyKey;
import io.joss.bgp.rib.PeerRoutingInformationBaseManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rainer
 *
 */

@Slf4j
@RequiredArgsConstructor
public class RoutingInstance
{

  private final AddressFamilyRoutingInstance familyInstanceProvider;
  private final PeerRoutingInformationBaseManager pribManager;
  private String firstPeerName;
  private String secondPeerName;
  private RoutingInstanceState state = RoutingInstanceState.STOPPED;

  private List<AddressFamilyRoutingInstance> familyInstances = new LinkedList<AddressFamilyRoutingInstance>();

  void configure(final RoutingInstanceConfiguration instConfig)
  {
    this.setFirstPeerName(instConfig.getFirstPeer().getPeerName());
    this.setSecondPeerName(instConfig.getSecondPeer().getPeerName());

    final Map<AddressFamilyKey, AddressFamilyRoutingPeerConfiguration> firstFamilyRouting = new HashMap<AddressFamilyKey, AddressFamilyRoutingPeerConfiguration>();
    final Map<AddressFamilyKey, AddressFamilyRoutingPeerConfiguration> secondFamilyRouting = new HashMap<AddressFamilyKey, AddressFamilyRoutingPeerConfiguration>();
    final Set<AddressFamilyKey> wantedFamilies = new HashSet<AddressFamilyKey>();

    for (final AddressFamilyRoutingPeerConfiguration afrfc : instConfig.getFirstPeer().getAddressFamilyConfigrations())
    {
      firstFamilyRouting.put(afrfc.getAddressFamilyKey(), afrfc);
      wantedFamilies.add(afrfc.getAddressFamilyKey());
    }
    for (final AddressFamilyRoutingPeerConfiguration afrfc : instConfig.getSecondPeer().getAddressFamilyConfigrations())
    {
      secondFamilyRouting.put(afrfc.getAddressFamilyKey(), afrfc);
      wantedFamilies.add(afrfc.getAddressFamilyKey());
    }

    for (final AddressFamilyKey afk : wantedFamilies)
    {
      final AddressFamilyRoutingInstance instance = this.familyInstanceProvider;
      instance.configure(afk, firstFamilyRouting.get(afk), secondFamilyRouting.get(afk));
      this.familyInstances.add(instance);
    }

    this.familyInstances = Collections.unmodifiableList(this.familyInstances);
  }

  void startInstance()
  {
    if (this.pribManager.isPeerRoutingInformationBaseAvailable(this.getFirstPeerName()))
    {
      if (this.pribManager.isPeerRoutingInformationBaseAvailable(this.getSecondPeerName()))
      {
        this.state = RoutingInstanceState.STARTING;

        for (final AddressFamilyRoutingInstance instance : this.getFamilyInstances())
        {
          this.log.info("Starting routing instance for " + instance.getAddressFamilyKey());

          try
          {
            instance.startInstance(this.pribManager.peerRoutingInformationBase(this.getFirstPeerName()),
                this.pribManager.peerRoutingInformationBase(this.getSecondPeerName()));

            if (instance.getState() != RoutingInstanceState.RUNNING)
            {
              this.log.error("failed to routing instance for " + instance.getAddressFamilyKey() + " with state " + instance.getState());
              this.state = RoutingInstanceState.PARTLY_RUNNING;
            }
          }
          catch (final Throwable t)
          {
            this.log.error("failed to routing instance for " + instance.getAddressFamilyKey(), t);

            this.state = RoutingInstanceState.PARTLY_RUNNING;
          }
        }

        if (this.state == RoutingInstanceState.STARTING)
        {
          this.state = RoutingInstanceState.RUNNING;
        }
      }
      else
      {
        this.state = RoutingInstanceState.PEER_ROUTING_BASE_UNAVAILABLE;
      }
    }
    else
    {
      this.state = RoutingInstanceState.PEER_ROUTING_BASE_UNAVAILABLE;
    }
  }

  void stopInstance()
  {
    for (final AddressFamilyRoutingInstance instance : this.getFamilyInstances())
    {
      this.log.info("Stopping routing instance for " + instance.getAddressFamilyKey());

      try
      {
        instance.stopInstance();
      }
      catch (final Throwable t)
      {
        this.log.error("failed to routing instance for " + instance.getAddressFamilyKey(), t);
      }
    }
  }

  /**
   * @return the firstPeerName
   */
  public String getFirstPeerName()
  {
    return this.firstPeerName;
  }

  /**
   * @param firstPeerName
   *          the firstPeerName to set
   */
  private void setFirstPeerName(final String firstPeerName)
  {
    this.firstPeerName = firstPeerName;
  }

  /**
   * @return the secondPeerName
   */
  public String getSecondPeerName()
  {
    return this.secondPeerName;
  }

  /**
   * @param secondPeerName
   *          the secondPeerName to set
   */
  private void setSecondPeerName(final String secondPeerName)
  {
    this.secondPeerName = secondPeerName;
  }

  /**
   * @return the familyInstances
   */
  public List<AddressFamilyRoutingInstance> getFamilyInstances()
  {
    return this.familyInstances;
  }

  /**
   * @return the state
   */
  public RoutingInstanceState getState()
  {
    return this.state;
  }
}
