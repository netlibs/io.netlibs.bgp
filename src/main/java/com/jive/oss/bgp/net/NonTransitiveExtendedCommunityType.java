package com.jive.oss.bgp.net;

public enum NonTransitiveExtendedCommunityType
{
  TWO_OCTET_AS_SPECIFIC,
  IPv4_ADDRESS_SPECIFIC,
  FOUR_OCTET_AS_SPECIFIC,
  OPAQUE,
  QOS_MARKING;
  
  public byte toCode(){
    switch(this){
      case TWO_OCTET_AS_SPECIFIC:
        return (byte) 0x40;
      case IPv4_ADDRESS_SPECIFIC:
        return (byte) 0x41;
      case FOUR_OCTET_AS_SPECIFIC:
        return (byte) 0x42;
      case OPAQUE:
        return (byte) 0x43;
      case QOS_MARKING:
        return (byte) 0x44;
      default:
        throw new IllegalArgumentException("unknown non-transitive community type: " + this);
    }
  }
  
  public static NonTransitiveExtendedCommunityType fromCode(byte code){
    switch(code){
      case 0x40:
        return TWO_OCTET_AS_SPECIFIC;
      case 0x41:
        return IPv4_ADDRESS_SPECIFIC;
      case 0x42:
        return FOUR_OCTET_AS_SPECIFIC;
      case 0x43:
        return OPAQUE;
      case 0x44:
        return QOS_MARKING;
      default:
        throw new IllegalArgumentException("unknown non-transitive community value: " + code);
    }
  }
}
