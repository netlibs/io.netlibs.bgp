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
package org.bgp4j.netty.protocol.open;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class ByteArrayUnsupportedCapabilityNotificationPacket extends UnsupportedCapabilityNotificationPacket
{

  private final byte[] capability;

  public ByteArrayUnsupportedCapabilityNotificationPacket(final byte[] capability)
  {
    this.capability = capability;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.NotificationPacket#encodeAdditionalPayload()
   */
  @Override
  protected ByteBuf encodeAdditionalPayload()
  {
    final ByteBuf buffer = Unpooled.buffer(this.capability.length);

    buffer.writeBytes(this.capability);

    return buffer;
  }

}
