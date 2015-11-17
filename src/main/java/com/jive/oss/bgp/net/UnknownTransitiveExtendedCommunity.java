package com.jive.oss.bgp.net;

import com.jive.oss.bgp.net.attributes.AbstractExtendedCommunityInterface;

import lombok.Getter;

public class UnknownTransitiveExtendedCommunity implements AbstractExtendedCommunityInterface
{
  
  @Getter
  private byte type;
  
  @Getter
  private byte[] data;
  
  public UnknownTransitiveExtendedCommunity(TransitiveExtendedCommunityType type, byte[] data)
  {
    this.type = type.toCode();
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
  public String humanReadable()
  {
    return new String("Unknown Transitive Extended Community (" + (int) this.type + "): " + this.data);
  }
  
  

}
