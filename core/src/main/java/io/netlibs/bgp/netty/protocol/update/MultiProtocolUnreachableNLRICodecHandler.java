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
 * File: org.bgp4j.netty.protocol.update.MultiProtocolUnreachableNLRICodecHandler.java
 */
package io.netlibs.bgp.netty.protocol.update;

import io.netlibs.bgp.net.NetworkLayerReachabilityInformation;
import io.netlibs.bgp.net.attributes.MultiProtocolUnreachableNLRI;
import io.netlibs.bgp.netty.BGPv4Constants;
import io.netlibs.bgp.netty.NLRICodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class MultiProtocolUnreachableNLRICodecHandler extends
PathAttributeCodecHandler<MultiProtocolUnreachableNLRI>
{

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.update.PathAttributeCodecHandler#typeCode(org.bgp4j.netty.protocol.update.PathAttribute)
   */
  @Override
  public int typeCode(final MultiProtocolUnreachableNLRI attr)
  {
    return BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_MP_UNREACH_NLRI;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.update.PathAttributeCodecHandler#valueLength(org.bgp4j.netty.protocol.update.PathAttribute)
   */
  @Override
  public int valueLength(final MultiProtocolUnreachableNLRI attr)
  {
    int size = 3; // 2 octets AFI + 1 octet SAFI

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
  public ByteBuf encodeValue(final MultiProtocolUnreachableNLRI attr)
  {
    final ByteBuf buffer = Unpooled.buffer(this.valueLength(attr));

    buffer.writeShort(attr.getAddressFamily().toCode());
    buffer.writeByte(attr.getSubsequentAddressFamily().toCode());

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
