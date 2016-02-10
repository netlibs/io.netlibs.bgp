package io.joss.bgp.net;

public enum TransitiveIPv4AddressSpecificExtCommSubTypes
{
  ROUTE_TARGET,
  ROUTE_ORIGIN,
  OSPF_DOMAIN_IDENTIFIER,
  OSPF_ROUTE_ID,
  L2VPN_IDENTIFIER,
  VRF_ROUTE_IMPORT,
  CISCO_VPN_DISTINGUISHER,
  INTER_AREA_P2MP_SEGMENTED_NEXT_HOP;
  
  public byte toCode(){
    switch(this){
      case ROUTE_TARGET:
        return 0x02;
      case ROUTE_ORIGIN:
        return 0x03;
      case OSPF_DOMAIN_IDENTIFIER:
        return 0x04;
      case OSPF_ROUTE_ID:
        return 0x07;
      case L2VPN_IDENTIFIER:
        return 0x0a;
      case VRF_ROUTE_IMPORT:
        return 0x0b;
      case INTER_AREA_P2MP_SEGMENTED_NEXT_HOP:
        return 0x12;
      default:
        throw new IllegalArgumentException("unknown IPv4 address speific extended community subtype: " + this);
    }
  }
  
  public static TransitiveIPv4AddressSpecificExtCommSubTypes fromCode(int code){
    switch(code){
      case 0x02:
        return ROUTE_TARGET;
      case 0x03:
        return ROUTE_ORIGIN;
      case 0x04:
        return OSPF_DOMAIN_IDENTIFIER;
      case 0x07:
        return OSPF_ROUTE_ID;
      case 0x0a:
        return L2VPN_IDENTIFIER;
      case 0x0b:
        return VRF_ROUTE_IMPORT;
      case 0x12:
        return INTER_AREA_P2MP_SEGMENTED_NEXT_HOP;
      default:
        throw new IllegalArgumentException("unknown IPv4 address specific extended community subtype: " + code);
    }
  }
}
