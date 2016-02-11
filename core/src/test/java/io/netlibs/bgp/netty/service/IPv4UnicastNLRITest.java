package io.netlibs.bgp.netty.service;

import static org.junit.Assert.*;

import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.net.InetAddresses;

import io.netlibs.bgp.net.attributes.IPv4UnicastNLRI;

public class IPv4UnicastNLRITest
{

  @Test
  public void IPv4UnicastNLRI_t1() throws UnknownHostException
  {
    // check that correct addresses are created
    // using explicit prefix length constructor
    byte[] buf = { (byte) 192, 0, 2 };
    IPv4UnicastNLRI data = new IPv4UnicastNLRI(24, buf);
    
    Assert.assertEquals(data.getAddress().getPrefixLength(), 24);
    Assert.assertEquals(data.getInetAddress(), InetAddresses.forString("192.0.2.0"));
  }
  
  @Test
  public void IPv4UnicastNLRI_t2() throws UnknownHostException
  {
    // check that host bits are masked when specifying the prefix length shorter than supplied address
    // using explicit prefix length constructor
    byte[] buf = { (byte) 192, 0, 2, 1 };
    IPv4UnicastNLRI data = new IPv4UnicastNLRI(24, buf);
    
    Assert.assertEquals(data.getAddress().getPrefixLength(), 24);
    Assert.assertEquals(data.getInetAddress(), InetAddresses.forString("192.0.2.0"));
  }
  
  @Test
  public void IPv4UnicastNLRI_t3() throws UnknownHostException
  {
    // check that correct addresses are created
    // using implied prefix-len constructor
    byte[] buf = { (byte) 172, 16, 24 };
    IPv4UnicastNLRI data = new IPv4UnicastNLRI(buf);
    
    Assert.assertEquals(data.getAddress().getPrefixLength(), 24);
    Assert.assertEquals(data.getInetAddress(), InetAddresses.forString("172.16.24.0"));
  }


  
}
