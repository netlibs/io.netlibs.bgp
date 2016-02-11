package io.netlibs.bgp.protocol.attributes;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public interface AbstractExtendedCommunityWithSubTypeInterface extends AbstractExtendedCommunityInterface
{
  byte getSubType();
  
  default byte[] getExtCommunityBytes(){
    ByteBuf data = Unpooled.buffer();
    data.writeByte(this.getType());
    data.writeByte(this.getSubType());
    data.writeBytes(this.getBytes());
    byte[] buf = new byte[data.readableBytes()];
    data.readBytes(buf);
    return buf;
  }
}
