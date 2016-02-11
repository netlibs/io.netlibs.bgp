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
package io.netlibs.bgp.netty.protocol.open;

import io.netlibs.bgp.BGPv4Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class UnsupportedVersionNumberNotificationPacket extends OpenNotificationPacket
{

  private int supportedProtocolVersion = BGPv4Constants.BGP_VERSION;

  public UnsupportedVersionNumberNotificationPacket()
  {
    super(OpenNotificationPacket.SUBCODE_UNSUPPORTED_VERSION_NUMBER);
  }

  public UnsupportedVersionNumberNotificationPacket(final int version)
  {
    super(OpenNotificationPacket.SUBCODE_UNSUPPORTED_VERSION_NUMBER);

    this.supportedProtocolVersion = version;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.NotificationPacket#encodePayload()
   */
  @Override
  protected ByteBuf encodeAdditionalPayload()
  {
    final ByteBuf buffer = Unpooled.buffer(2);

    buffer.writeShort(this.supportedProtocolVersion);

    return buffer;
  }

  /**
   * @return the version
   */
  public int getSupportedProtocolVersion()
  {
    return this.supportedProtocolVersion;
  }

}
