package io.netlibs.bgp.netty.protocol.update;

import io.netlibs.bgp.protocol.BGPv4Constants;
import io.netlibs.bgp.protocol.attributes.AbstractExtendedCommunityInterface;
import io.netlibs.bgp.protocol.attributes.ExtendedCommunityPathAttribute;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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
