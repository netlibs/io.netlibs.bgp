package com.jive.oss.bgp.netty.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPv6MPLSLabelNLRI extends AbstractMPLSLabelNLRI
{
  public IPv6MPLSLabelNLRI(byte[] data)
  {
    super(data);
  }

  public InetAddress getInetAddress() throws UnknownHostException
  {
    byte[] pfx = this.getAddress().getPrefix();
    
    byte[] data = new byte[16];
    if(pfx.length != 4){
      for(int i=0; i<pfx.length; i++)
        data[i] = pfx[i];
      for(int i=pfx.length; i<16; i++)
        data[i] = 0;
    } else {
      data = pfx;
    }
    return InetAddress.getByAddress(data);
  }
}