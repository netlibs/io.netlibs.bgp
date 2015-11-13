package com.jive.oss.bgp.netty.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.jive.oss.commons.ip.CidrV4Address;

public class IPv4MPLSVPNNLRI extends AbstractMPLSLabelledVPNNLRI
{

  public IPv4MPLSVPNNLRI(byte[] data) throws UnknownHostException
  {
    super(data);
  }
  
  public IPv4MPLSVPNNLRI(int label, AbstractRouteDistinguisherType rd, int prefixlen, byte[] data){
    super(label, rd, prefixlen, data);
    System.err.printf("label: %d, rd: %s, pfxln: %d, data: %s\n", label, rd, prefixlen, data);
  }

  public InetAddress getInetAddress() throws UnknownHostException
  {
    byte[] data = IPv4UnicastNLRI.nlriTo32BIPv4(this.getNlri().getPrefix());
    return InetAddress.getByAddress(data);
  }

  public static IPv4MPLSVPNNLRI fromCidrV4AddressRDAndLabel(CidrV4Address cidr, AbstractRouteDistinguisherType rd, int label)
  {
    byte[] prefix = IPv4UnicastNLRI.longTo32BIPv4(cidr.prefix());
    return new IPv4MPLSVPNNLRI(label, rd, cidr.mask(), prefix); 
  }

}