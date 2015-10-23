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
 * File: org.bgp4j.netty.protocol.update.NextHopPathAttributeCodecHandler.java
 */
package com.jive.oss.bgp.netty.protocol.update;

import com.jive.oss.bgp.net.attributes.NextHopPathAttribute;
import com.jive.oss.bgp.netty.BGPv4Constants;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class NextHopPathAttributeCodecHandler extends PathAttributeCodecHandler<NextHopPathAttribute>
{

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.update.PathAttributeCodecHandler#typeCode(org.bgp4j.netty.protocol.update.PathAttribute)
   */
  @Override
  public int typeCode(final NextHopPathAttribute attr)
  {
    return BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_NEXT_HOP;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.update.PathAttributeCodecHandler#valueLength(org.bgp4j.netty.protocol.update.PathAttribute)
   */
  @Override
  public int valueLength(final NextHopPathAttribute attr)
  {
    return 4;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.update.PathAttributeCodecHandler#encodeValue(org.bgp4j.netty.protocol.update.PathAttribute)
   */

  @Override
  public ByteBuf encodeValue(final NextHopPathAttribute attr)
  {
    final ByteBuf buffer = Unpooled.buffer(4);
    buffer.writeBytes(attr.getNextHop().getAddress().getAddress());
    return buffer;
  }

}
