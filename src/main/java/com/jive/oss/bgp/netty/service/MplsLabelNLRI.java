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

  @Getter
  private boolean bos;

  public MplsLabelNLRI(final byte[] data)
  {

    // first 3 bytes are the label
    // last byte indicates TC and BOS bits. Currently indicated as 1.
    this.label = Ints.fromBytes(data[0], data[1], data[2], (byte) 0);
    this.label >>= 12;
    this.bos = (data[3] & 1) == 0;
    
    // find all but the first 3 bytes and convert them to the NLRI
    final byte[] pt = new byte[data.length - 3];
    System.arraycopy(data, 3, pt, 0, data.length - 3);

    this.address = new NetworkLayerReachabilityInformation((data.length - 3) * 8, pt);

  }

}
