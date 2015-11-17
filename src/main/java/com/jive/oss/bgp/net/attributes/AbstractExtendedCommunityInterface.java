package com.jive.oss.bgp.net.attributes;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public interface AbstractExtendedCommunityInterface
{ 
  byte[] getBytes();
  byte getType();
  
  String humanReadable();
  
  default byte[] getExtCommunityBytes(){
    ByteBuf data = Unpooled.buffer();
    data.writeByte(this.getType());
    data.writeBytes(this.getBytes());
    byte[] buf = new byte[data.readableBytes()];
    data.readBytes(buf);
    return buf;
  }
}
