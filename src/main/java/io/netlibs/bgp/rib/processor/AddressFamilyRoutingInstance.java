/**
 * 
 */
package io.netlibs.bgp.rib.processor;

import org.slf4j.Logger;

import io.netlibs.bgp.config.nodes.AddressFamilyRoutingPeerConfiguration;
import io.netlibs.bgp.net.AddressFamilyKey;
import io.netlibs.bgp.net.RIBSide;
import io.netlibs.bgp.rib.PeerRoutingInformationBase;
import io.netlibs.bgp.rib.RoutingInformationBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rainer
 *
 */

@RequiredArgsConstructor
@Slf4j
public class AddressFamilyRoutingInstance
{

  private final RouteTransportListener firstListener;
  private final RouteTransportListener secondListener;

  private AddressFamilyKey addressFamilyKey;
  private RoutingInstanceState state = RoutingInstanceState.STOPPED;

  void configure(AddressFamilyKey addressFamilyKey, AddressFamilyRoutingPeerConfiguration firstConfig, AddressFamilyRoutingPeerConfiguration secondConfig)
  {
    this.addressFamilyKey = addressFamilyKey;

    if (firstConfig != null)
      firstListener.configure(firstConfig.getLocalRoutingFilters(), firstConfig.getLocalDefaultPathAttributes());
    if (secondConfig != null)
      secondListener.configure(secondConfig.getLocalRoutingFilters(), secondConfig.getLocalDefaultPathAttributes());
  }

  void startInstance(PeerRoutingInformationBase firstPeerRIB, PeerRoutingInformationBase secondPeerRIB)
  {
    RoutingInformationBase firstLocal = firstPeerRIB.routingBase(RIBSide.Local, getAddressFamilyKey());
    RoutingInformationBase firstRemote = firstPeerRIB.routingBase(RIBSide.Remote, getAddressFamilyKey());
    RoutingInformationBase secondLocal = secondPeerRIB.routingBase(RIBSide.Local, getAddressFamilyKey());
    RoutingInformationBase secondRemote = secondPeerRIB.routingBase(RIBSide.Remote, getAddressFamilyKey());
    boolean connected = false;

    state = RoutingInstanceState.STARTING;

    if (firstLocal != null && secondRemote != null)
    {
      log.info("connect second remote to first local");

      firstListener.setTarget(firstLocal);
      firstListener.setSource(secondRemote);
      secondRemote.addPerRibListener(firstListener);
      connected = true;
    }
    if (secondLocal != null && firstRemote != null)
    {
      log.info("connect first remote to second local");

      secondListener.setTarget(secondLocal);
      secondListener.setSource(firstRemote);
      firstRemote.addPerRibListener(secondListener);
      connected = true;
    }

    if (connected)
      state = RoutingInstanceState.RUNNING;
    else
      state = RoutingInstanceState.PEER_ROUTING_BASE_UNAVAILABLE;
  }

  void stopInstance()
  {
    if (firstListener.getSource() != null)
      firstListener.getSource().removePerRibListener(firstListener);
    firstListener.setTarget(null);
    if (secondListener.getSource() != null)
      secondListener.getSource().removePerRibListener(secondListener);
    secondListener.setTarget(null);

    state = RoutingInstanceState.STOPPED;
  }

  /**
   * @return the addressFamilyKey
   */
  public AddressFamilyKey getAddressFamilyKey()
  {
    return addressFamilyKey;
  }

  /**
   * @return the state
   */
  public RoutingInstanceState getState()
  {
    return state;
  }
}
