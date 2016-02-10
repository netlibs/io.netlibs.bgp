package io.joss.bgp.net;

public enum TransitiveTwoOctetASSpecificExtCommSubTypes
{
  ROUTE_TARGET,
  ROUTE_ORIGIN,
  OSPF_DOMAIN_IDENTIFIER,
  BGP_DATA_COLLECTION,
  SOURCE_AS,
  L2VPN_IDENTIFIER,
  CISCO_VPN_DISTINGUISHER;
  
  public byte toCode(){
    switch(this){
      case ROUTE_TARGET:
        return 0x02;
      case ROUTE_ORIGIN:
        return 0x03;
      case OSPF_DOMAIN_IDENTIFIER:
        return 0x05;
      case BGP_DATA_COLLECTION:
        return 0x08;
      case SOURCE_AS:
        return 0x09;
      case L2VPN_IDENTIFIER:
        return 0x0A;
      case CISCO_VPN_DISTINGUISHER:
        return 0x10;
      default:
        throw new IllegalArgumentException("unknown two-octet AS specific extended community sub-type: " + this);
    }
  }
  
  public static TransitiveTwoOctetASSpecificExtCommSubTypes fromCode(byte code){
    switch(code){
      case 0x02:
        return ROUTE_TARGET;
      case 0x03:
        return ROUTE_ORIGIN;
      case 0x05:
        return OSPF_DOMAIN_IDENTIFIER;
      case 0x08:
        return BGP_DATA_COLLECTION;
      case 0x09:
        return SOURCE_AS;
      case 0x0A:
        return L2VPN_IDENTIFIER;
      case 0x10:
        return CISCO_VPN_DISTINGUISHER;
      default:
          throw new IllegalArgumentException("unknown two-octet AS specific extended community sub-type: " + code);
    }
  }
  
}
