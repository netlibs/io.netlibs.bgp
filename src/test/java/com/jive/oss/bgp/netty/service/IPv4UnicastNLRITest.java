package com.jive.oss.bgp.netty.service;

import static org.junit.Assert.*;

import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.net.InetAddresses;

public class IPv4UnicastNLRITest
{

  @Test
  public void IPv4UnicastNLRI_t1() throws UnknownHostException
  {
    // check that correct addresses are created
    byte[] buf = { 24, (byte) 192, 0, 2, 1};
    IPv4UnicastNLRI data = new IPv4UnicastNLRI(buf);
    
    Assert.assertEquals(data.getAddress().getNlriLengthAsInt(), 24);
    Assert.assertEquals(data.getAddress().getNlriPrefixAsInetAddress(), InetAddresses.forString("192.0.2.1"));
  }

}
