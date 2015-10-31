package com.jive.oss.bgp.netty.service;

import com.google.common.base.Preconditions;
import com.jive.oss.bgp.net.NetworkLayerReachabilityInformation;

import lombok.Getter;

public class IPv6UnicastNLRI
{
  @Getter
  private NetworkLayerReachabilityInformation address;

  public IPv6UnicastNLRI(byte[] data)
  {
    Preconditions.checkArgument(data.length == 17, "invalid data length");
    this.address = new NetworkLayerReachabilityInformation(data);
  }

}
