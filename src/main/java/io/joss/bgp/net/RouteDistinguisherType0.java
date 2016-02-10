package io.joss.bgp.net;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Value;

@Value
public class RouteDistinguisherType0 extends AbstractTwoByteASNFourByteAdministratorRDCommunityType implements AbstractRouteDistinguisherType
{ 

  // Type 0:
  //  Administrator Subfield: 2-bytes, ASN
  //  Assigned Number Subfield: 4-bytes, arbitrary number
  // ref: RFC4364
  
  public RouteDistinguisherType0(int administrator, long assigned_number)
  {
    super(administrator, assigned_number);
  }

  @Override
  public byte[] getType()
  {
    return new byte[] { 0, 0 };
  }

  public static RouteDistinguisherType0 fromBytes(byte[] data)
  {
    // get first 2-bytes and convert to an integer
    int read_administrator = Ints.fromBytes((byte) 0, (byte) 0, data[0], data[1]);
    // get subsequent 4-bytes and convert to an integer
    long read_assigned_number = Ints.fromBytes(data[2], data[3], data[4], data[5]);
    
    return new RouteDistinguisherType0(read_administrator, read_assigned_number);
  }

  public String humanReadable()
  {
    return new String(this.getAdministrator() + ":" + this.getAssignedNumber());
  }
}