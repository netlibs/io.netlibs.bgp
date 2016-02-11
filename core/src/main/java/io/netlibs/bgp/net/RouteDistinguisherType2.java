package io.netlibs.bgp.net;

import com.google.common.primitives.Ints;

import lombok.Value;

@Value
public class RouteDistinguisherType2 extends AbstractFourByteASNTwoByteAdministratorRDCommunityType implements AbstractRouteDistinguisherType
{

  // Type 2 RD:
  //  Administrator subfield: 4-bytes, 32b ASN
  //  Assigned Number subfield: 2-bytes, arbitrary number
  // ref: RFC4364

  public RouteDistinguisherType2(long administrator, int assigned_number)
  {
    super(administrator, assigned_number);
  }

  @Override
  public byte[] getType()
  {
    return new byte[] { 0, 2 };
  }
  
  public static RouteDistinguisherType2 fromBytes(byte[] data)
  {
    long read_administrator = Ints.fromBytes(data[0], data[1], data[2], data[3]);
    int read_assigned_number = Ints.fromBytes((byte) 0, (byte) 0, data[4], data[5]);
    return new RouteDistinguisherType2(read_administrator, read_assigned_number);
  }
  
  public String humanReadable(){
    return new String(this.getAdministrator() + ":" + this.getAssignedNumber());
  }
}