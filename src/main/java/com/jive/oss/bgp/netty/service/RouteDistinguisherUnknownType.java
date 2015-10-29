package com.jive.oss.bgp.netty.service;

import lombok.Getter;

public class RouteDistinguisherUnknownType implements RouteDistinguisherType {
  
  @Getter
  byte[] type;
  
  @Getter
  byte[] value;
  
  public RouteDistinguisherUnknownType(byte[] type, byte[] value)
  {
    this.type = type;
    this.value = value;
  }
  
  @Override
  public byte[] getBytes()
  {
    byte[] buf = new byte[8];
    System.arraycopy(this.type, 0, buf, 0, 2);
    System.arraycopy(this.value, 0, buf, 2, 6); 
    return buf;
  }
  
  public static RouteDistinguisherUnknownType fromBytes(byte[] data)
  {
    byte[] type = new byte[2];
    byte[] value = new byte[6];
    
    System.arraycopy(data, 0, type, 0, 2);
    System.arraycopy(data, 2, value, 0, 6);
  
    return new RouteDistinguisherUnknownType(type, value);
  }
  
}