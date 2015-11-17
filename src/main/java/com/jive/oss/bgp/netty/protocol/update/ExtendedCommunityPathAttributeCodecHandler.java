package com.jive.oss.bgp.netty.protocol.update;

import com.jive.oss.bgp.net.attributes.AbstractExtendedCommunityInterface;
import com.jive.oss.bgp.net.attributes.ExtendedCommunityPathAttribute;
import com.jive.oss.bgp.netty.BGPv4Constants;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledDirectByteBuf;

public class ExtendedCommunityPathAttributeCodecHandler extends PathAttributeCodecHandler<ExtendedCommunityPathAttribute>
{

  @Override
  public int typeCode(ExtendedCommunityPathAttribute attr)
  {
    return BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_EXTENDED_COMMUNITIES;
  }

  @Override
  public int valueLength(ExtendedCommunityPathAttribute attr)
  {
    int size=0;
    
    if (attr.getMembers() != null)
      size += 8 * attr.getMembers().size();
    
    return size;
  }

  @Override
  public ByteBuf encodeValue(ExtendedCommunityPathAttribute attr)
  {
    ByteBuf buffer = Unpooled.buffer(this.valueLength(attr));
    if (attr.getMembers() != null){
      for(AbstractExtendedCommunityInterface extcomm: attr.getMembers()){
        buffer.writeBytes(extcomm.getExtCommunityBytes());
      }
    }
    return buffer;
  }

}
