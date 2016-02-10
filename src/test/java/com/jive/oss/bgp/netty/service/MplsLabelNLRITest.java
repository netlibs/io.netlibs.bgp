package com.jive.oss.bgp.netty.service;

import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.net.InetAddresses;
import com.jive.oss.commons.ip.CidrV4Address;

import io.netlibs.bgp.net.attributes.IPv4MPLSLabelNLRI;

public class MplsLabelNLRITest
{

  @Test
  public void IPv4LU_t1() throws UnknownHostException
  {
    // test case where we create a value by specifying the raw instance
    byte[] buf = new byte[] { 0, 0, (byte) 241, 4, 3, 2, 1 };
    IPv4MPLSLabelNLRI data = new IPv4MPLSLabelNLRI(buf);
    
    Assert.assertEquals(data.getInetAddress(), InetAddresses.forString("4.3.2.1"));
    Assert.assertEquals(data.getAddress().getPrefixLength(), 32);
    Assert.assertEquals(data.getLabel(), 15);
    Assert.assertEquals(data.isBos(), true);
  }
  
  @Test
  public void IPv4LU_t2() throws UnknownHostException
  {
    // test case where we create a value by using the fromCidrV4Address method
    // which also tests the constructor
    IPv4MPLSLabelNLRI data = IPv4MPLSLabelNLRI.fromCidrV4AddressAndLabel(CidrV4Address.fromString("192.168.12.0/24"), 42, false);
    
    Assert.assertEquals(data.getInetAddress(), InetAddresses.forString("192.168.12.0"));
    Assert.assertEquals(data.getAddress().getPrefixLength(), 24);
    Assert.assertEquals(data.getLabel(), 42);
    Assert.assertEquals(data.isBos(), false);
  }

}
