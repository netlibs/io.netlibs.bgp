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
 * File: org.bgp4j.netty.protocol.update.AggregatorPathAttributeCodecHandler.java
 */
package com.jive.oss.bgp.netty.protocol.update;

import com.jive.oss.bgp.net.attributes.AggregatorPathAttribute;
import com.jive.oss.bgp.netty.BGPv4Constants;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class AggregatorPathAttributeCodecHandler extends PathAttributeCodecHandler<AggregatorPathAttribute>
{

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.update.PathAttributeCodecHandler#typeCode(org.bgp4j.netty.protocol.update.PathAttribute)
   */
  @Override
  public int typeCode(final AggregatorPathAttribute attr)
  {
    return (attr.isFourByteASNumber()
        ? BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_AS4_AGGREGATOR
            : BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_AGGREGATOR);
  }

  @Override
  public int valueLength(final AggregatorPathAttribute attr)
  {
    return (attr.isFourByteASNumber() ? 8 : 6);
  }

  @Override
  public ByteBuf encodeValue(final AggregatorPathAttribute attr)
  {
    final ByteBuf buffer = Unpooled.buffer(this.valueLength(attr));

    if (attr.isFourByteASNumber())
    {
      buffer.writeInt(attr.getAsNumber());
    }
    else
    {
      buffer.writeShort(attr.getAsNumber());
    }

    buffer.writeBytes(attr.getAggregator().getAddress());

    return buffer;
  }

}
