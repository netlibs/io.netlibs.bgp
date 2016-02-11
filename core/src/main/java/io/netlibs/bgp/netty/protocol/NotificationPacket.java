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
package io.netlibs.bgp.netty.protocol;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.netlibs.bgp.protocol.BGPv4Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class NotificationPacket extends BGPv4Packet
{
  private final int errorCode;
  private final int errorSubcode;

  protected NotificationPacket(final int errorCode, final int errorSubcode)
  {
    this.errorCode = errorCode;
    this.errorSubcode = errorSubcode;
  }

  @Override
  protected final ByteBuf encodePayload()
  {
    final ByteBuf buffer = Unpooled.buffer(BGPv4Constants.BGP_PACKET_MAX_LENGTH);

    buffer.writeByte(this.errorCode);
    buffer.writeByte(this.errorSubcode);

    final ByteBuf additionalPayload = this.encodeAdditionalPayload();

    if (additionalPayload != null)
    {
      buffer.writeBytes(additionalPayload);
    }

    return buffer;
  }

  @Override
  public final int getType()
  {
    return BGPv4Constants.BGP_PACKET_TYPE_NOTIFICATION;
  }

  /**
   * @return the errorCode
   */
  public final int getErrorCode()
  {
    return this.errorCode;
  }

  /**
   * @return the errorSubcode
   */
  public final int getErrorSubcode()
  {
    return this.errorSubcode;
  }

  protected ByteBuf encodeAdditionalPayload()
  {
    return null;
  }

  @Override
  public <T> T apply(final BGPv4PacketVisitor<T> visitor)
  {
    return visitor.visit(this);
  }

  @Override
  public String toString()
  {
    return (new ToStringBuilder(this))
        .append("type", this.getType())
        .append("errorCode", this.errorCode)
        .append("errorSubcode", this.errorSubcode).toString();
  }
}
