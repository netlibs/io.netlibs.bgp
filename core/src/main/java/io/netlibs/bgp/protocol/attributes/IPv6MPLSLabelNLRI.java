package io.netlibs.bgp.protocol.attributes;

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.netlibs.ipaddr.CidrV6Address;

public class IPv6MPLSLabelNLRI extends AbstractMPLSLabelNLRI
{
  public IPv6MPLSLabelNLRI(byte[] data)
  {
    super(data);
  }
  
  public IPv6MPLSLabelNLRI(int label, boolean bos, int pfxlen, byte[] data)
  {
    super(label, bos, pfxlen, data);
  }

  public InetAddress getInetAddress() throws UnknownHostException
  {
    byte[] data = IPv6UnicastNLRI.nlriTo128BIPv6(this.getAddress().getPrefix());
    return InetAddress.getByAddress(data);
  }
  
  public static IPv6MPLSLabelNLRI fromCidrV6AddressAndLabel(CidrV6Address cidr, int label, boolean bos){
    byte[] prefix = IPv6UnicastNLRI.bigIntegerTo128BIPv6(cidr.prefix());
    return new IPv6MPLSLabelNLRI(label, bos, cidr.mask(), prefix);
  }
}