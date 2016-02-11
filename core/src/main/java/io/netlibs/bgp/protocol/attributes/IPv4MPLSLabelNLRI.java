package io.netlibs.bgp.protocol.attributes;

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.netlibs.ipaddr.CidrV4Address;
import lombok.ToString;

@ToString
public class IPv4MPLSLabelNLRI extends AbstractMPLSLabelNLRI
{
  public IPv4MPLSLabelNLRI(byte[] data)
  {
    // Call parent class' constructor directly
    super(data);
  }
  
  public IPv4MPLSLabelNLRI(int label, boolean bos, int pfxlen, byte[] data){
    // Call parent class' constructor directly
    super(label, bos, pfxlen, data);
  }

  public InetAddress getInetAddress() throws UnknownHostException
  {
    byte[] data = IPv4UnicastNLRI.nlriTo32BIPv4(this.getAddress().getPrefix());
    return InetAddress.getByAddress(data);
  }
  
  public static IPv4MPLSLabelNLRI fromCidrV4AddressAndLabel(CidrV4Address cidr, int label, boolean bos){
    byte[] prefix = IPv4UnicastNLRI.longTo32BIPv4(cidr.prefix());
    return new IPv4MPLSLabelNLRI(label, bos, cidr.mask(), prefix);
  }
  
}
