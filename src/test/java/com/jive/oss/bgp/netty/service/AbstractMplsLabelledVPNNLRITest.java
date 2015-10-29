package com.jive.oss.bgp.netty.service;

import static org.junit.Assert.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.net.InetAddresses;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class AbstractMplsLabelledVPNNLRITest
{

  @Test
  public void type0_encode_test()
  {
    RouteDistinguisherType0 data = new RouteDistinguisherType0(1, 1);
    byte[] val = data.getBytes();
    Assert.assertArrayEquals(val, new byte[] { 0, 1, 0, 0 ,0, 1 });   
  }
  
  @Test
  public void type1_encode_test() throws UnknownHostException
  {
    RouteDistinguisherType1  data = new RouteDistinguisherType1((Inet4Address) InetAddresses.fromLittleEndianByteArray(new byte[] {1,1,1,1}), 1);
    byte[] val = data.getBytes();
    Assert.assertArrayEquals(val, new byte[] { 1, 1, 1, 1, 0, 1 });
  }
  
  @Test
  public void type2_encode_test()
  {
    RouteDistinguisherType2 data = new RouteDistinguisherType2(65535, 1);
    byte[] val = data.getBytes();
  
    // Go learn about two's complement if this does not make sense (I did) :-)
    Assert.assertArrayEquals(val, new byte[] { 0, 0, -1, -1, 0, 1});
  }
  
  @Test
  public void type0_decode_test()
  {
    // encode 1:1 and check whether it decodes to the correct parts
    byte[] buf = { 0, 1, 0, 0, 0, 1 };
    RouteDistinguisherType0 data = RouteDistinguisherType0.fromBytes(buf);
    
    Assert.assertEquals(data.getAssigned_number(), 1);
    Assert.assertEquals(data.getAdministrator(), 1);
  }
  
  @Test
  public void type1_decode_test() throws UnknownHostException
  {
    // encode 1.1.1.1:1
    byte[] buf = { 1, 1, 1, 1, 0, 1};
    RouteDistinguisherType1 data = RouteDistinguisherType1.fromBytes(buf);
    
    Assert.assertEquals(data.getAdministrator(), (Inet4Address) InetAddresses.forString("1.1.1.1"));
    Assert.assertEquals(data.getAssigned_number(), 1);
  }
  
  @Test
  public void type2_decode_test() {
    // encode 65536:1
    byte[] buf = { 0, 1, 0, 0, 0, 1 };
    RouteDistinguisherType2 data = RouteDistinguisherType2.fromBytes(buf);
    
    Assert.assertEquals(data.getAdministrator(), 65536);
    Assert.assertEquals(data.getAssigned_number(), 1);    
  }
}
