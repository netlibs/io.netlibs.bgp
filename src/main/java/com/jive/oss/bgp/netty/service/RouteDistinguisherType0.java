package com.jive.oss.bgp.netty.service;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Value;

@Value
public class RouteDistinguisherType0 implements RouteDistinguisherType
{ 
  private int administrator;
  private long assigned_number;

  // Type 0:
  //  Administrator Subfield: 2-bytes, ASN
  //  Assigned Number Subfield: 4-bytes, arbitrary number
  // ref: RFC4364
  
  public RouteDistinguisherType0(int administrator, long assigned_number)
  {
    Preconditions.checkArgument(administrator > 0 || administrator < 65536, "Invalid administrator");
    Preconditions.checkArgument(assigned_number > 0 || assigned_number < 4294967296L, "Invalid assigned_number");
    this.administrator = administrator; 
    this.assigned_number = assigned_number;
  }

  @Override
  public byte[] getBytes()
  {
    ByteBuf data = Unpooled.buffer();
    data.writeShort(this.administrator);
    data.writeInt((int) this.assigned_number);
    byte[] buf = new byte[data.readableBytes()];
    data.readBytes(buf);
    return buf;
  }

  public static RouteDistinguisherType0 fromBytes(byte[] data)
  {
    // get first 2-bytes and convert to an integer
    int read_administrator = Ints.fromBytes((byte) 0, (byte) 0, data[0], data[1]);
    // get subsequent 4-bytes and convert to an integer
    long read_assigned_number = Ints.fromBytes(data[2], data[3], data[4], data[5]);
    
    return new RouteDistinguisherType0(read_administrator, read_assigned_number);
  }
}