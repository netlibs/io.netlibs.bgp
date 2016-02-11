package io.netlibs.bgp.protocol;

import io.netlibs.bgp.protocol.attributes.AbstractExtendedCommunityWithSubTypeInterface;
import lombok.Getter;

public class UnknownTransitiveTwoByteASNSpecificExtendedCommunity implements AbstractExtendedCommunityWithSubTypeInterface 
{
  
  @Getter
  private byte type;
  
  @Getter
  private byte subtype;
  
  @Getter
  private byte[] data;

  public UnknownTransitiveTwoByteASNSpecificExtendedCommunity(TransitiveExtendedCommunityType type, TransitiveTwoOctetASSpecificExtCommSubTypes subtype, byte[] data)
  {
    this.type = type.toCode();
    this.subtype = subtype.toCode();
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
    return new String("Unknown Transitive Two-Byte ASN Specific Extended Community: " + this.data);
  }
  
}
