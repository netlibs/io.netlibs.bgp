package com.jive.oss.bgp.netty.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPv4MPLSVPNNLRI extends AbstractMPLSLabelledVPNNLRI
{

  public IPv4MPLSVPNNLRI(byte[] data) throws UnknownHostException
  {
    super(data);
  }

  public InetAddress getInetAddress() throws UnknownHostException
  {
    byte[] data = IPv4UnicastNLRI.nlriTo32BIPv4(this.getNlri().getPrefix());
    return InetAddress.getByAddress(data);
  }

}