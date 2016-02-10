package io.joss.bgp.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;
import com.google.common.primitives.Ints;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Value;

@Value
public class RouteDistinguisherType1 extends AbstractIPv4AddressTwoByteAdministratorRDCommunityType implements AbstractRouteDistinguisherType
{
 
  // Type 1:
  //  Administrator Subfield: 4-bytes, IPAddr
  //  Assigned Number Subfield: 2-bytes, arbitrary number
  // ref: RFC4364

  public RouteDistinguisherType1(Inet4Address administrator, int assigned_number)
  {
    super(administrator, assigned_number);
  }

  @Override
  public byte[] getType()
  {
    return new byte[] { 0, 1 };
  }
  
  public static RouteDistinguisherType1 fromBytes(byte[] data) throws UnknownHostException
  {
    byte[] admin_part = new byte[4];  
    
    // read first 4 bytes
    System.arraycopy(data, 0, admin_part, 0, 4);
    Inet4Address read_administrator = (Inet4Address) InetAddress.getByAddress(admin_part);
    int read_assigned_number = Ints.fromBytes((byte) 0, (byte) 0, data[4], data[5]);
    return new RouteDistinguisherType1(read_administrator, read_assigned_number);
  }
  
  public String humanReadable(){
    return new String(this.getAdministrator() + ":" + this.getAssignedNumber());
  }
}