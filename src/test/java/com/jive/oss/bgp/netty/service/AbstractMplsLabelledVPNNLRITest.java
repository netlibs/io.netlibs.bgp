package com.jive.oss.bgp.netty.service;

import static org.junit.Assert.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.net.InetAddresses;
import com.google.common.primitives.Ints;
import com.jive.oss.bgp.net.NetworkLayerReachabilityInformation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class AbstractMplsLabelledVPNNLRITest
{

  @Test
  public void type0_encode_test()
  {
    RouteDistinguisherType0 data = new RouteDistinguisherType0(2, 1);
    byte[] val = data.getBytes();
    Assert.assertArrayEquals(val, new byte[] { 0, 2, 0, 0 ,0, 1 });   
  }
  
  @Test
  public void type1_encode_test() throws UnknownHostException
  {
    RouteDistinguisherType1  data = new RouteDistinguisherType1((Inet4Address) InetAddresses.fromLittleEndianByteArray(new byte[] {4,3,2,1}), 1);
    byte[] val = data.getBytes();
    Assert.assertArrayEquals(val, new byte[] { 1, 2, 3, 4, 0, 1 });
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
    // encode 4:1 and check whether it decodes to the correct parts
    byte[] buf = { 0, 4, 0, 0, 0, 1 };
    RouteDistinguisherType0 data = RouteDistinguisherType0.fromBytes(buf);
    
    Assert.assertEquals(data.getAssigned_number(), 1);
    Assert.assertEquals(data.getAdministrator(), 4);
  }
  
  @Test
  public void type1_decode_test() throws UnknownHostException
  {
    // encode 1.2.3.4:5
    byte[] buf = { 1, 2, 3, 4, 0, 5};
    RouteDistinguisherType1 data = RouteDistinguisherType1.fromBytes(buf);
    
    Assert.assertEquals(data.getAdministrator(), (Inet4Address) InetAddresses.forString("1.2.3.4"));
    Assert.assertEquals(data.getAssigned_number(), 5);
  }
  
  @Test
  public void type2_decode_test() {
    // encode 65536:1
    byte[] buf = { 0, 1, 0, 0, 0, 1 };
    RouteDistinguisherType2 data = RouteDistinguisherType2.fromBytes(buf);
    
    Assert.assertEquals(data.getAdministrator(), 65536);
    Assert.assertEquals(data.getAssigned_number(), 1);    
  }
  
  @Test
  public void abstract_type0_decode() throws UnknownHostException
  {
    // Format is:
    //    Label (3byte)
    //      20-bit label, 3-bit TOS, 1-bit BOS
    //    RD:
    //      Type (2-byte)
    //      Value (6-byte)
    //    Prefix
    
    // byte[2] = 0b10000001 -> Label 8, BOS bit set
    byte[] buf = { 0, 0, (byte) 129, 0, 0, 0, 2, 0, 0, 0, 1, 32, 1, 2, 3, 4 };
    AbstractMplsLabelledVPNNLRI data = new AbstractMplsLabelledVPNNLRI(buf);
    
    Assert.assertEquals(data.isBos(), true);
    Assert.assertEquals(data.getRd(), new RouteDistinguisherType0(2, 1));
    Assert.assertEquals(data.getNlri().getNlriLengthAsInt(), 32);
    Assert.assertEquals(data.getNlri().getNlriPrefixAsInetAddress(), InetAddresses.forString("1.2.3.4"));
    Assert.assertEquals(data.getLabel(), 8);
  }
  
  @Test
  public void abstract_type1_decode() throws UnknownHostException 
  {
    byte[] buf = { 0, 0, (byte) 129, 0, 1, 1, 2, 3, 4, 0, 1, 32, 1, 2, 3, 4 };
    AbstractMplsLabelledVPNNLRI data = new AbstractMplsLabelledVPNNLRI(buf);
    
    Assert.assertEquals(data.isBos(), true);
    Assert.assertEquals(data.getRd(), new RouteDistinguisherType1((Inet4Address) InetAddresses.forString("1.2.3.4"), 1));
    Assert.assertEquals(data.getNlri().getNlriLengthAsInt(), 32);
    Assert.assertEquals(data.getNlri().getNlriPrefixAsInetAddress(), InetAddresses.forString("1.2.3.4"));
    Assert.assertEquals(data.getLabel(), 8);    
  }
  
  @Test
  public void abstract_type2_decode() throws UnknownHostException 
  {
    byte[] buf = { 0, 0, (byte) 129, 0, 2, 0, 0, (byte) 255, (byte) 255, 0, 1, 32, 1, 2, 3, 4 };
    AbstractMplsLabelledVPNNLRI data = new AbstractMplsLabelledVPNNLRI(buf);
    
    Assert.assertEquals(data.isBos(), true);
    Assert.assertEquals(data.getRd(), new RouteDistinguisherType2((long) 65535, 1));
    Assert.assertEquals(data.getNlri().getNlriLengthAsInt(), 32);
    Assert.assertEquals(data.getNlri().getNlriPrefixAsInetAddress(), InetAddresses.forString("1.2.3.4"));
    Assert.assertEquals(data.getLabel(), 8);
  }
  
  @Test
  public void abstract_type_0_ipv6_decode() throws UnknownHostException
  {
    byte[] buf = { 0, 0, (byte) 129, 0, 0, 0, 1, 0, 0, 0, 2, (byte) 128, 32, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
    AbstractMplsLabelledVPNNLRI data = new AbstractMplsLabelledVPNNLRI(buf);
    
    Assert.assertEquals(data.isBos(), true);
    Assert.assertEquals(data.getRd(), new RouteDistinguisherType0(1,2));
    Assert.assertEquals(data.getNlri().getNlriLengthAsInt(), 128);
    Assert.assertEquals(data.getNlri().getNlriPrefixAsInetAddress(), InetAddresses.forString("2001::1"));
  }
  
  @Test
  public void abstract_type_1_ipv6_decode() throws UnknownHostException
  {
    byte[] buf = { 0, 0, (byte) 129, 0, 1, 1, 2, 3, 4, 0, 1, (byte) 128, 32, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
    AbstractMplsLabelledVPNNLRI data = new AbstractMplsLabelledVPNNLRI(buf);
    
    Assert.assertEquals(data.isBos(), true);
    Assert.assertEquals(data.getRd(), new RouteDistinguisherType1((Inet4Address) InetAddresses.forString("1.2.3.4"), 1));
    Assert.assertEquals(data.getNlri().getNlriLengthAsInt(), 128);
    Assert.assertEquals(data.getNlri().getNlriPrefixAsInetAddress(), InetAddresses.forString("2001::1"));
  }
  
  @Test
  public void abstract_type_2_ipv6_decode() throws UnknownHostException
  {
    byte[] buf = { 0, 0, (byte) 129, 0, 2, 0, 0, (byte) 255, (byte) 255, 0, 1, (byte) 128, 32, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
    AbstractMplsLabelledVPNNLRI data = new AbstractMplsLabelledVPNNLRI(buf);
    
    Assert.assertEquals(data.isBos(), true);
    Assert.assertEquals(data.getRd(), new RouteDistinguisherType2((long) 65535, 1));
    Assert.assertEquals(data.getNlri().getNlriLengthAsInt(), 128);
    Assert.assertEquals(data.getNlri().getNlriPrefixAsInetAddress(), InetAddresses.forString("2001::1"));
  }
}
