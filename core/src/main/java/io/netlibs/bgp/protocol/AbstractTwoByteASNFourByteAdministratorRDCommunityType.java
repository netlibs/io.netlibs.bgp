package io.netlibs.bgp.protocol;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;

public class AbstractTwoByteASNFourByteAdministratorRDCommunityType
{
  @Getter
  private int administrator;
  
  @Getter
  private long assignedNumber;

  public AbstractTwoByteASNFourByteAdministratorRDCommunityType(int administrator, long assignedNumber){
    Preconditions.checkArgument(administrator > 0 || administrator < 65536, "Invalid administrator");
    Preconditions.checkArgument(assignedNumber < 0 || assignedNumber < 4294967296L, "Invalid assignedNumber");
    
    this.administrator = administrator;
    this.assignedNumber = assignedNumber;
  }
  
  public byte[] getBytes()
  {
    ByteBuf data = Unpooled.buffer();
    data.writeShort(this.administrator);
    data.writeInt((int) this.assignedNumber);
    byte[] buf = new byte[data.readableBytes()];
    // transfers the data buffer to the buf array
    data.readBytes(buf);
    return buf;
  }
  
  public String humanReadable(){
    StringBuilder s = new StringBuilder();
    s.append(this.administrator);
    s.append(":");
    s.append(this.assignedNumber);
    return s.toString();
  }
}
