package com.jive.oss.bgp.netty.service;

import static org.junit.Assert.*;

import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.net.InetAddresses;

import io.netlibs.bgp.net.attributes.IPv6UnicastNLRI;

public class IPv6UnicastNLRITest
{

  @Test
  public void IPv6UnicastNLRI_t1() throws UnknownHostException
  {
    byte[] buf = { (byte) 128, 32, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
    IPv6UnicastNLRI data = new IPv6UnicastNLRI(buf);
    
    //Assert.assertEquals(data.getAddress().getNlriLengthAsInt(), 128);
    //Assert.assertEquals(data.getAddress().getNlriPrefixAsInetAddress(), InetAddresses.forString("2001::1"));
  }

}
