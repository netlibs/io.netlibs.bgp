package org.bgp4j.config.global;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;

import org.bgp4j.config.nodes.PeerConfiguration;
import org.bgp4j.config.nodes.RoutingProcessorConfiguration;

public class ApplicationConfiguration
{

  private final List<PeerConfiguration> peers;

  public ApplicationConfiguration()
  {
    this.peers = new LinkedList<>();
  }


  public List<PeerConfiguration> listPeerConfigurations()
  {
    return this.peers;
  }


  public void addPeer(final PeerConfiguration config)
  {
    this.peers.add(config);
  }

  public SocketAddress getServerPort()
  {
    return new InetSocketAddress(1179);
  }

  public RoutingProcessorConfiguration getRoutingProcessorConfiguration()
  {
    // TODO Auto-generated method stub
    return null;
  }

}
