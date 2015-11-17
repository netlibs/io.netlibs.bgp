package com.jive.oss.bgp.net;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;

public class AbstractFourByteASNTwoByteAdministratorRDCommunityType
{
 
  @Getter
  private long administrator;
  
  @Getter
  private int assignedNumber;

  public AbstractFourByteASNTwoByteAdministratorRDCommunityType(long administrator, int assignedNumber){
    Preconditions.checkArgument(administrator > 0  || administrator < 4294967296L, "Invalid administrator");
    Preconditions.checkArgument(assignedNumber > 0 || assignedNumber < 65536, "Invalid assigned_number");
    this.administrator = administrator;
    this.assignedNumber = assignedNumber;     
  }
  
  public byte[] getBytes()
  {
    ByteBuf data = Unpooled.buffer();
    data.writeInt((int) this.administrator);
    data.writeShort(this.assignedNumber);
    byte[] buf = new byte[data.readableBytes()];
    data.readBytes(buf);  
    return buf;
  } 
}
