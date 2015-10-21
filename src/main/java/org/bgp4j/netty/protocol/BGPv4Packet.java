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
 */
package org.bgp4j.netty.protocol;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bgp4j.netty.BGPv4Constants;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * base class for all BGPv4 protocol packets
 *
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public abstract class BGPv4Packet
{
  /**
   * build a binary representation of the protocol packet
   *
   * @return the encoded packet
   */
  public ByteBuf encodePacket()
  {
    return this.wrapBufferHeader(this.encodePayload(), this.getType());
  }

  /**
   * encode the specific packet-type payload
   *
   * @return the encoded packet payload
   */
  protected abstract ByteBuf encodePayload();

  /**
   * obtain the BGP packet type code.
   *
   * @return
   */
  public abstract int getType();

  /**
   * wrap the BGP payload in a BGPv4 header field
   *
   * @param wrapped
   *          the packet payload
   * @param type
   *          the packet type code
   * @return the completely assembled BGPv4 packet
   */
  private ByteBuf wrapBufferHeader(final ByteBuf wrapped, final int type)
  {
    final int wrappedSize = (wrapped != null) ? wrapped.readableBytes() : 0;
    final ByteBuf buffer = Unpooled.buffer(wrappedSize + BGPv4Constants.BGP_PACKET_HEADER_LENGTH);

    for (int i = 0; i < BGPv4Constants.BGP_PACKET_MARKER_LENGTH; i++)
    {
      buffer.writeByte(0xff);
    }

    buffer.writeShort(wrappedSize + BGPv4Constants.BGP_PACKET_HEADER_LENGTH);
    buffer.writeByte(type);

    if (wrapped != null)
    {
      buffer.writeBytes(wrapped);
    }

    return buffer;
  }

  @Override
  public String toString()
  {
    return (new ToStringBuilder(this)).append("type", this.getType()).toString();
  }
}
