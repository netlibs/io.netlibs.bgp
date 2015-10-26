package com.jive.oss.bgp.netty.service;

import com.google.common.primitives.Ints;
import com.jive.oss.bgp.net.NetworkLayerReachabilityInformation;

import lombok.Getter;
import lombok.ToString;

@ToString
public class MplsLabelNLRI
{

  @Getter
  private int label;

  @Getter
  private final NetworkLayerReachabilityInformation address;

  public MplsLabelNLRI(final byte[] data)
  {

    // first 3 bytes are the label
    this.label = Ints.fromBytes(data[0], data[1], data[2], (byte) 0);
    this.label >>= 12;

    //
    final byte[] pt = new byte[data.length - 3];
    System.arraycopy(data, 3, pt, 0, data.length - 3);

    this.address = new NetworkLayerReachabilityInformation((data.length - 3) * 8, pt);

  }

}
