package com.jive.oss.bgp.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.google.common.net.InetAddresses;
import com.google.common.primitives.Ints;
import com.jive.oss.bgp.net.attributes.AbstractExtendedCommunityInterface;
import com.jive.oss.bgp.net.attributes.AbstractExtendedCommunityWithSubTypeInterface;

public class TransitiveIPv4AddressTwoByteAdministratorRT extends AbstractIPv4AddressTwoByteAdministratorRDCommunityType implements AbstractExtendedCommunityWithSubTypeInterface
{

  public TransitiveIPv4AddressTwoByteAdministratorRT(Inet4Address administrator, int assignedNumber)
  {
    super(administrator, assignedNumber);
  }

  @Override
  public byte getType()
  {
    return TransitiveExtendedCommunityType.TWO_OCTET_IPv4_ADDRESS_SPECIFIC.toCode();
  }

  @Override
  public byte getSubType()
  {
    return TransitiveIPv4AddressSpecificExtCommSubTypes.ROUTE_TARGET.toCode();
  }
  
  public static TransitiveIPv4AddressTwoByteAdministratorRT fromBytes(byte[] data) throws UnknownHostException{
    byte[] admin_part = new byte[4];  
    System.arraycopy(data, 0, admin_part, 0, 4);
    Inet4Address read_administrator = (Inet4Address) InetAddress.getByAddress(admin_part);
    int read_assigned_number = Ints.fromBytes((byte) 0, (byte) 0, data[4], data[5]);
    return new TransitiveIPv4AddressTwoByteAdministratorRT(read_administrator, read_assigned_number);
  }

  @Override
  public String humanReadable(){
    return new String("RT:" + this.getAdministrator() + ":" + this.getAssignedNumber());
  }
}
