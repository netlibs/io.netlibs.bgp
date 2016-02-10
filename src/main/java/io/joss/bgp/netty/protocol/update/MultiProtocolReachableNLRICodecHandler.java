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
package io.joss.bgp.netty.protocol.update;

import io.joss.bgp.net.NetworkLayerReachabilityInformation;
import io.joss.bgp.net.attributes.MultiProtocolReachableNLRI;
import io.joss.bgp.netty.BGPv4Constants;
import io.joss.bgp.netty.NLRICodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class MultiProtocolReachableNLRICodecHandler extends PathAttributeCodecHandler<MultiProtocolReachableNLRI>
{

  @Override
  public int typeCode(final MultiProtocolReachableNLRI attr)
  {
    return BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_MP_REACH_NLRI;
  }

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

        if ((nlri.getPrefix() != null) && (nlri.getPrefixLength() == nlri.getPrefix().length))
        {
          size += nlri.getPrefixLength();
        }
        else
        {
          size += NLRICodec.calculateEncodedNLRILength(nlri);
        }

      }

    }

    return size;
  }

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
        if ((nlri.getPrefix() != null) && (nlri.getPrefixLength() == nlri.getPrefix().length))
        {
          buffer.writeByte(nlri.getPrefixLength());
          buffer.writeBytes(nlri.getPrefix());
        }
        else
        {
          buffer.writeBytes(NLRICodec.encodeNLRI(nlri));
        }
      }
    }
    return buffer;
  }

}
