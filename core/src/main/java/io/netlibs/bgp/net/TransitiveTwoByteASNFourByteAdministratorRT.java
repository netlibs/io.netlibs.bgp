package io.netlibs.bgp.net;

import com.google.common.primitives.Ints;

import io.netlibs.bgp.net.attributes.AbstractExtendedCommunityWithSubTypeInterface;

public class TransitiveTwoByteASNFourByteAdministratorRT extends AbstractTwoByteASNFourByteAdministratorRDCommunityType implements AbstractExtendedCommunityWithSubTypeInterface
{

  public TransitiveTwoByteASNFourByteAdministratorRT(int administrator, long assignedNumber)
  {
    super(administrator, assignedNumber);
  }

  @Override
  public byte getType()
  {
    return TransitiveExtendedCommunityType.TWO_OCTET_AS_SPECIFIC.toCode();
  }

  @Override
  public byte getSubType()
  {
    return TransitiveTwoOctetASSpecificExtCommSubTypes.ROUTE_TARGET.toCode();
  }
  
  public static TransitiveTwoByteASNFourByteAdministratorRT fromBytes(byte[] data)
  {
    int readAdministrator = Ints.fromBytes((byte) 0, (byte) 0, data[0], data[1]);
    long readAdministrativeNumber = Ints.fromBytes(data[2], data[3], data[4], data[5]);
    
    return new TransitiveTwoByteASNFourByteAdministratorRT(readAdministrator, readAdministrativeNumber);
  }
  
  @Override
  public String humanReadable(){
    return new String("RT:" + this.getAdministrator() + ":" + this.getAssignedNumber());
  }
}
