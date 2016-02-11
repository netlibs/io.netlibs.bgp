package io.netlibs.bgp.net;


import java.net.Inet4Address;
import java.net.UnknownHostException;

import javax.sound.midi.SysexMessage;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.net.InetAddresses;

import io.netlibs.bgp.net.TransitiveExtendedCommunityType;
import io.netlibs.bgp.net.TransitiveIPv4AddressSpecificExtCommSubTypes;
import io.netlibs.bgp.net.TransitiveIPv4AddressTwoByteAdministratorRT;
import io.netlibs.bgp.net.TransitiveTwoByteASNFourByteAdministratorRT;
import io.netlibs.bgp.net.TransitiveTwoOctetASSpecificExtCommSubTypes;

public class ExtendedCommunityTests
{

  @Test
  public void TransitiveRTTest_TwoByteASN_FourByteAdmin()
  {
    TransitiveTwoByteASNFourByteAdministratorRT rt = 
        TransitiveTwoByteASNFourByteAdministratorRT.fromBytes(new byte[] { (byte) 0x19, (byte) 0xF3, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF });
   
    // check decoding
    Assert.assertEquals(rt.getType(), TransitiveExtendedCommunityType.TWO_OCTET_AS_SPECIFIC.toCode());
    Assert.assertEquals(rt.getSubType(), TransitiveTwoOctetASSpecificExtCommSubTypes.ROUTE_TARGET.toCode());
    Assert.assertEquals(rt.getAdministrator(), 6643);
    Assert.assertEquals(rt.getAssignedNumber(), 65535L);
    
    
    // check encoding
    Assert.assertArrayEquals(rt.getBytes(), new byte[] { (byte) 0x19, (byte) 0xF3, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF });
    Assert.assertArrayEquals(rt.getExtCommunityBytes(), new byte[] { (byte) 0x00, (byte) 0x02, (byte) 0x19, (byte) 0xF3, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF }); 
  }
  
  @Test
  public void TransitiveRTTest_FourByteInetAddress_TwoByteAdmin() throws UnknownHostException{
    TransitiveIPv4AddressTwoByteAdministratorRT rt = 
        TransitiveIPv4AddressTwoByteAdministratorRT.fromBytes(new byte[] { (byte) 192, (byte) 168, (byte) 123, (byte) 1, (byte) 0xFF, (byte) 0xFF });
    
    // check decoding
    Assert.assertEquals(rt.getType(), TransitiveExtendedCommunityType.TWO_OCTET_IPv4_ADDRESS_SPECIFIC.toCode());
    Assert.assertEquals(rt.getSubType(), TransitiveIPv4AddressSpecificExtCommSubTypes.ROUTE_TARGET.toCode());
    Assert.assertEquals(rt.getAdministrator(), (Inet4Address) InetAddresses.forString("192.168.123.1"));
    
    // check encoding
    Assert.assertArrayEquals(rt.getBytes(), new byte[] { (byte) 192, (byte) 168, (byte) 123, (byte) 1, (byte) 0xFF, (byte) 0xFF });
    Assert.assertArrayEquals(rt.getExtCommunityBytes(), new byte[] { (byte) 0x01, (byte) 0x02, (byte) 192, (byte) 168, (byte) 123, (byte) 1, (byte) 0xFF, (byte) 0xFF });
  }
  
  @Test
  public void TransitiveNonTransitive_check(){
    // value is 0b0100 0001 -  this is transitive
    byte chk = (byte) 0x41;
    
    // output this
    String chks = String.format("%8s", Integer.toBinaryString(chk & 0xFF)).replace(' ', '0');
    System.err.println(chks);
    
    // shift right 6, and check whether the remaining bit is set
    System.err.println(String.format("%8s", Integer.toBinaryString(chk >> 6)).replace(' ', '0'));
    System.err.println(((chk >> 6) & 1) == 1);
    Assert.assertEquals(((chk >> 6) & 1) == 1, true);
    
    // clear bits 7&8
    System.err.println(String.format("%8s",  Integer.toBinaryString((byte) ~(3 << 6))).replace(' ', '0'));
    System.err.println(String.format("%8s", Integer.toBinaryString((byte) chk & (~(3 << 6)))).replace(' ', '0'));
    Assert.assertEquals((byte) chk & (~(3 << 6)), 0x01);
  }

}
