package com.jive.oss.bgp.net;

import com.jive.oss.bgp.net.attributes.AbstractExtendedCommunityWithSubTypeInterface;

import lombok.Getter;

public class UnknownTransitiveIPv4AddressSpecificExtendedCommunity implements AbstractExtendedCommunityWithSubTypeInterface
{
  
  @Getter
  private byte type;
  
  @Getter
  private byte subtype;
  
  @Getter
  private byte[] data;

  public UnknownTransitiveIPv4AddressSpecificExtendedCommunity(TransitiveExtendedCommunityType type, TransitiveIPv4AddressSpecificExtCommSubTypes subtype, byte[] data)
  {
    this.type = type.toCode();
    this.type = subtype.toCode();
    this.data = data;
  }
  
  @Override
  public byte[] getBytes()
  {
    return this.data;
  }

  @Override
  public byte getType()
  {
    return this.type;
  }

  @Override
  public byte getSubType()
  {
    return this.subtype;
  }

  @Override
  public String humanReadable()
  {
    return new String("Unknown IPv4 Address Specific Extended Communtity (" + (int) this.type + ", " + (int) this.subtype + "): " + this.data);
  } 
  
  
}
