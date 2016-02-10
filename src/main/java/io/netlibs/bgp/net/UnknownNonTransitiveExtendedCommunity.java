package io.netlibs.bgp.net;

import io.netlibs.bgp.net.attributes.AbstractExtendedCommunityInterface;
import lombok.Getter;

public class UnknownNonTransitiveExtendedCommunity implements AbstractExtendedCommunityInterface
{
  
  @Getter
  private byte[] data;
  
  @Getter
  private byte type;
  
  public UnknownNonTransitiveExtendedCommunity(NonTransitiveExtendedCommunityType type, byte[] data)
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
    return new String("Unknown non-transitive Extended Community (" + (int) this.type + "): " + this.data);
  }

}
