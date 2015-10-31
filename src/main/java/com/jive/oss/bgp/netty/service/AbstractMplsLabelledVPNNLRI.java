package com.jive.oss.bgp.netty.service;

import java.net.UnknownHostException;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import com.jive.oss.bgp.net.NetworkLayerReachabilityInformation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class AbstractMplsLabelledVPNNLRI
{
  @Getter
  private int label;
  
  @Getter
  private boolean bos;

  @Getter
  private RouteDistinguisherType rd;

  @Getter
  private NetworkLayerReachabilityInformation nlri;
  
  public AbstractMplsLabelledVPNNLRI(byte[] data) throws UnknownHostException
  {
    // Format
    // Label: 3-bytes
    // RD:
    //    2-byte type
    //    6-byte value
    // Address:
    //    Remaining bytes
    
    this.label = Ints.fromBytes(data[0], data[1], data[2], (byte) 0);
    this.label >>= 12;    // bit-shift such that this is an int
    this.bos = (data[3] & 1) == 0;
    
    int rd_type = Ints.fromBytes((byte) 0, (byte) 0, data[3], data[4]);
    
    // Extract the RD contents
    byte[] rd_content = new byte[6];
    System.arraycopy(data, 5, rd_content, 0, 6);

    switch(rd_type)
    {
      case 0:
        this.rd = RouteDistinguisherType0.fromBytes(rd_content);
        break;
      case 1:
        this.rd = RouteDistinguisherType1.fromBytes(rd_content);
        break;
      case 2:
        this.rd = RouteDistinguisherType2.fromBytes(rd_content);
        break;
      default:
        this.rd = RouteDistinguisherUnknownType.fromBytes(rd_content);
        break;
    }
    
    // Remaining bytes are the IP prefix - minus the 3-byte label, and 8-byte
    // (RDType,RD) tuple.
    byte[] prefix = new byte[data.length - 3 - 8];
    System.arraycopy(data, 11, prefix, 0, data.length - 11);
    this.nlri = new NetworkLayerReachabilityInformation((data.length - 11) * 8, prefix);
  }
}
