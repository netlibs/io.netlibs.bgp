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
 * File: org.bgp4j.netty.protocol.update.UnknownPathAttributeCodecHandler.java
 */
package io.joss.bgp.netty.protocol.update;

import io.joss.bgp.net.attributes.UnknownPathAttribute;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class UnknownPathAttributeCodecHandler extends
PathAttributeCodecHandler<UnknownPathAttribute>
{

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.update.PathAttributeCodecHandler#typeCode(org.bgp4j.netty.protocol.update.PathAttribute)
   */
  @Override
  public int typeCode(final UnknownPathAttribute attr)
  {
    return attr.getTypeCode();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.update.PathAttributeCodecHandler#valueLength(org.bgp4j.netty.protocol.update.PathAttribute)
   */
  @Override
  public int valueLength(final UnknownPathAttribute attr)
  {
    if (attr.getValue() != null)
    {
      return attr.getValue().length;
    }
    else
    {
      return 0;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.update.PathAttributeCodecHandler#encodeValue(org.bgp4j.netty.protocol.update.PathAttribute)
   */
  @Override
  public ByteBuf encodeValue(final UnknownPathAttribute attr)
  {
    if (attr.getValue() != null)
    {
      final byte[] value = attr.getValue();
      final ByteBuf buffer = Unpooled.buffer(value.length);

      buffer.writeBytes(value);

      return buffer;

    }
    else
    {
      return null;
    }
  }

}
