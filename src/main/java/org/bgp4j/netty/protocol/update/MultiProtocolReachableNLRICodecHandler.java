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
 * File: org.bgp4j.netty.protocol.update.MultiProtocolReachableNLRICodecHandler.java
 */
package org.bgp4j.netty.protocol.update;

import org.bgp4j.net.NetworkLayerReachabilityInformation;
import org.bgp4j.net.attributes.MultiProtocolReachableNLRI;
import org.bgp4j.netty.BGPv4Constants;
import org.bgp4j.netty.NLRICodec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class MultiProtocolReachableNLRICodecHandler extends
PathAttributeCodecHandler<MultiProtocolReachableNLRI>
{

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.update.PathAttributeCodecHandler#typeCode(org.bgp4j.netty.protocol.update.PathAttribute)
   */
  @Override
  public int typeCode(final MultiProtocolReachableNLRI attr)
  {
    return BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_MP_REACH_NLRI;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.update.PathAttributeCodecHandler#valueLength(org.bgp4j.netty.protocol.update.PathAttribute)
   */
  @Override
  public int valueLength(final MultiProtocolReachableNLRI attr)
  {
    int size = 5; // 2 octets AFI + 1 octet SAFI + 1 octet NextHop address length + 1 octet reserved

    if (attr.getNextHop() != null)
    {
      size += attr.getNextHop().getAddress().length;
    }

    if (attr.getNlris() != null)
    {
      for (final NetworkLayerReachabilityInformation nlri : attr.getNlris())
      {
        size += NLRICodec.calculateEncodedNLRILength(nlri);
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
  public ByteBuf encodeValue(final MultiProtocolReachableNLRI attr)
  {
    final ByteBuf buffer = Unpooled.buffer(this.valueLength(attr));

    buffer.writeShort(attr.getAddressFamily().toCode());
    buffer.writeByte(attr.getSubsequentAddressFamily().toCode());

    if (attr.getNextHop() != null)
    {
      buffer.writeByte(attr.getNextHop().getAddress().length);
      buffer.writeBytes(attr.getNextHop().getAddress());
    }
    else
    {
      buffer.writeByte(0);
    }

    buffer.writeByte(0); // write reserved field

    if (attr.getNlris() != null)
    {
      for (final NetworkLayerReachabilityInformation nlri : attr.getNlris())
      {
        buffer.writeBytes(NLRICodec.encodeNLRI(nlri));
      }
    }

    return buffer;
  }

}
