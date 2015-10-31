package com.jive.oss.bgp.netty.service;

import java.net.InetAddress;

import com.google.common.base.Preconditions;
import com.jive.oss.bgp.net.NetworkLayerReachabilityInformation;

import lombok.Getter;

public class IPv4UnicastNLRI
{
  @Getter
  private NetworkLayerReachabilityInformation address;

  public IPv4UnicastNLRI(byte[] data)
  {
    Preconditions.checkArgument(data.length == 5, "invalid data length");
    this.address = new NetworkLayerReachabilityInformation(data);
  }
 
  
    //public static IPv4UnicastNLRI fromQualfiedInetAddress(QualifiedIPv4)
  //public static IPv4UnicastNLRI fromQualifiedInetAddress(QualifiedIPv4Address)
  //{
//
  //}
}
