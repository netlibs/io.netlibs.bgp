package com.jive.oss.bgp.net;

import java.net.Inet4Address;

import com.google.common.base.Preconditions;
import com.google.common.net.InetAddresses;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;

public class AbstractIPv4AddressTwoByteAdministratorRDCommunityType
{
  @Getter
  private int assignedNumber;
  
  @Getter
  private Inet4Address administrator;
  
  public AbstractIPv4AddressTwoByteAdministratorRDCommunityType(Inet4Address administrator, int assignedNumber){
    Preconditions.checkArgument(assignedNumber > 0 || assignedNumber < 65536, "Invalid assigned_number");
    this.assignedNumber = assignedNumber;
    this.administrator = administrator;
  }
  
  public byte[] getBytes()
  {
    ByteBuf data = Unpooled.buffer();
    data.writeInt(InetAddresses.coerceToInteger(administrator));
    data.writeShort(this.assignedNumber);
    byte[] buf = new byte[data.readableBytes()];
    data.readBytes(buf);
    return buf;
  }
  
  
  
}
