/**
 *  Copyright 2012 Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * File: org.bgp4j.netty.protocol.update.ASPathAttributeCodecHandler.java
 */
package org.bgp4j.netty.protocol.update;

import org.bgp4j.net.ASType;
import org.bgp4j.net.PathSegment;
import org.bgp4j.net.attributes.ASPathAttribute;
import org.bgp4j.netty.BGPv4Constants;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class ASPathAttributeCodecHandler extends PathAttributeCodecHandler<ASPathAttribute>
{

  static class PathSegmentCodec
  {
    static int getValueLength(final PathSegment segment)
    {
      int size = 2; // type + length field

      if ((segment.getAses() != null) && (segment.getAses().size() > 0))
      {
        size += segment.getAses().size() * (segment.getAsType() == ASType.AS_NUMBER_4OCTETS ? 4 : 2);
      }

      return size;
    }

    static ByteBuf encodeValue(final PathSegment segment)
    {
      final ByteBuf buffer = Unpooled.buffer(getValueLength(segment));

      buffer.writeByte(PathSegmentTypeCodec.toCode(segment.getPathSegmentType()));
      if ((segment.getAses() != null) && (segment.getAses().size() > 0))
      {
        buffer.writeByte(segment.getAses().size());

        for (final int as : segment.getAses())
        {
          if (segment.getAsType() == ASType.AS_NUMBER_4OCTETS)
          {
            buffer.writeInt(as);
          }
          else
          {
            buffer.writeShort(as);
          }
        }

      }
      else
      {
        buffer.writeByte(0);
      }
      return buffer;
    }

  }

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.update.PathAttributeCodecHandler#typeCode(org.bgp4j.netty.protocol.update.PathAttribute)
   */
  @Override
  public int typeCode(final ASPathAttribute attr)
  {
    return (attr.isFourByteASNumber()
        ? BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_AS4_PATH
            : BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_AS_PATH);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.update.PathAttributeCodecHandler#valueLength(org.bgp4j.netty.protocol.update.PathAttribute)
   */
  @Override
  public int valueLength(final ASPathAttribute attr)
  {
    int size = 0; // type + length field

    if (attr.getPathSegments() != null)
    {
      for (final PathSegment seg : attr.getPathSegments())
      {
        size += PathSegmentCodec.getValueLength(seg);
      }
    }

    return size;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.update.PathAttributeCodecHandler#encodeValue(org.bgp4j.netty.protocol.update.PathAttribute)
   */
  @Override
  public ByteBuf encodeValue(final ASPathAttribute attr)
  {
    final ByteBuf buffer = Unpooled.buffer(this.valueLength(attr));

    if ((attr.getPathSegments() != null) && (attr.getPathSegments().size() > 0))
    {
      for (final PathSegment seg : attr.getPathSegments())
      {
        buffer.writeBytes(PathSegmentCodec.encodeValue(seg));
      }
    }

    return buffer;
  }

}
