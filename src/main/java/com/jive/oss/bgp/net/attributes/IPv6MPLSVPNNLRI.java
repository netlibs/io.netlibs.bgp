package com.jive.oss.bgp.net.attributes;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.jive.oss.bgp.net.AbstractRouteDistinguisherType;
import com.jive.oss.commons.ip.CidrV6Address;

public class IPv6MPLSVPNNLRI extends AbstractMPLSLabelledVPNNLRI
{

  public IPv6MPLSVPNNLRI(byte[] data) throws UnknownHostException
  {
    super(data);
  }
  
  public IPv6MPLSVPNNLRI(int label, AbstractRouteDistinguisherType rd, int prefixlen, byte[] data)
  {
    super(label, rd, prefixlen, data);
  }
  
  public InetAddress getInetAddress() throws UnknownHostException {
    byte[] data = IPv6UnicastNLRI.nlriTo128BIPv6(this.getNlri().getPrefix());
    return InetAddress.getByAddress(data);
  }
  
  public static IPv6MPLSVPNNLRI fromCidrV6AddressRDAndLabel(CidrV6Address cidr, AbstractRouteDistinguisherType rd, int label){
    byte[] prefix = IPv6UnicastNLRI.bigIntegerTo128BIPv6(cidr.prefix());
    return new IPv6MPLSVPNNLRI(label, rd, cidr.mask(), prefix);
  }
  
}