package com.jive.oss.bgp.netty.service;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Value;

@Value class RouteDistinguisherType2 implements AbstractRouteDistinguisherType
{
  private long administrator;
  private int assigned_number;
  
  // Type 2 RD:
  //  Administrator subfield: 4-bytes, 32b ASN
  //  Assigned Number subfield: 2-bytes, arbitrary number
  // ref: RFC4364

  public RouteDistinguisherType2(long administrator, int assigned_number)
  {
    Preconditions.checkArgument(administrator > 0  || administrator < 4294967296L, "Invalid administrator");
    Preconditions.checkArgument(assigned_number > 0 || assigned_number < 65536, "Invalid assigned_number");
    this.administrator = administrator;
    this.assigned_number = assigned_number; 
  }

  @Override
  public byte[] getBytes()
  {
    ByteBuf data = Unpooled.buffer();
    data.writeInt((int) this.administrator);
    data.writeShort(this.assigned_number);
    byte[] buf = new byte[data.readableBytes()];
    data.readBytes(buf);  
    return buf;
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
  
}