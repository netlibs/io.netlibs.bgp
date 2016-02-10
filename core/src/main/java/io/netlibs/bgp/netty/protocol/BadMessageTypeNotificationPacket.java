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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class BadMessageTypeNotificationPacket extends MessageHeaderErrorNotificationPacket {

  private int unknownMessageType;

  public BadMessageTypeNotificationPacket() {
    super(MessageHeaderErrorNotificationPacket.SUBCODE_BAD_MESSAGE_LENGTH);
  }

  public BadMessageTypeNotificationPacket(final int type) {
    super(MessageHeaderErrorNotificationPacket.SUBCODE_BAD_MESSAGE_LENGTH);

    this.setUnknownMessageType(type);
  }

  /**
   * @return the length
   */
  public int getUnknownMessageType() {
    return this.unknownMessageType;
  }

  /**
   * @param length the length to set
   */
  public void setUnknownMessageType(final int length) {
    this.unknownMessageType = length;
  }

  /* (non-Javadoc)
   * @see org.bgp4j.netty.protocol.NotificationPacket#encodePayload()
   */
  @Override
  protected ByteBuf encodeAdditionalPayload()
  {
    final ByteBuf buffer = Unpooled.buffer(1);

    buffer.writeByte(this.getType());

    return buffer;
  }
}
