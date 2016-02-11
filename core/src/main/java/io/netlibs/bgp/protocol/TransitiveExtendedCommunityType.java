package io.netlibs.bgp.protocol;

public enum TransitiveExtendedCommunityType
{
  TWO_OCTET_AS_SPECIFIC,
  TWO_OCTET_IPv4_ADDRESS_SPECIFIC,
  FOUR_OCTET_AS_SPECIFIC,
  OPAQUE,
  QOS_MARKING,
  COS_CAPABILITY,
  EVPN,
  FLOW_SPEC_REDIRECT_MIRROR,
  GENERIC_EXPERIMENTAL,
  GENERIC_EXPERIMENTAL_PART_2,
  GENERIC_EXPERIMENTAL_PART_3;
  
  public byte toCode(){
    switch(this){
      case TWO_OCTET_AS_SPECIFIC:
        return (byte) 0x00;
      case TWO_OCTET_IPv4_ADDRESS_SPECIFIC:
        return (byte) 0x01;
      case FOUR_OCTET_AS_SPECIFIC:
        return (byte) 0x02;
      case OPAQUE:
        return (byte) 0x03;
      case QOS_MARKING:
        return (byte) 0x04;
      case COS_CAPABILITY:
        return (byte) 0x05;
      case EVPN:
        return (byte) 0x06;
      case FLOW_SPEC_REDIRECT_MIRROR:
        return (byte) 0x08;
      case GENERIC_EXPERIMENTAL:
        return (byte) 0x80;
      case GENERIC_EXPERIMENTAL_PART_2:
        return (byte) 0x81;
      case GENERIC_EXPERIMENTAL_PART_3:
        return (byte) 0x82;
      default:
        throw new IllegalArgumentException("unknown transitive extended community type: " + this);
    }
  }
  
  public static TransitiveExtendedCommunityType fromCode(byte code){
    switch(code){
      case 0x00:
        return TWO_OCTET_AS_SPECIFIC;
      case 0x01:
        return TWO_OCTET_IPv4_ADDRESS_SPECIFIC;
      case 0x02:
        return FOUR_OCTET_AS_SPECIFIC;
      case 0x03:
        return OPAQUE;
      case 0x04:
        return QOS_MARKING;
      case 0x05:
        return COS_CAPABILITY;
      case 0x06:
        return EVPN;
      case 0x08:
        return FLOW_SPEC_REDIRECT_MIRROR;
      case (byte) 0x80:
        return GENERIC_EXPERIMENTAL;
      case (byte) 0x81:
        return GENERIC_EXPERIMENTAL_PART_2;
      case (byte) 0x82:
        return GENERIC_EXPERIMENTAL_PART_3;
      default:
        throw new IllegalArgumentException("unknown transitive extended community code: " + code);
    }
  } 
}
