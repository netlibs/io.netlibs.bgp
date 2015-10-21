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
 * File: org.bgp4j.netty.protocol.open.CapabilityListUnsupportedCapabilityNotificationPacket.java
 */
package org.bgp4j.netty.protocol.open;

import java.util.List;

import org.bgp4j.net.capabilities.Capability;

import io.netty.buffer.ByteBuf;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class CapabilityListUnsupportedCapabilityNotificationPacket extends UnsupportedCapabilityNotificationPacket {

  private final List<Capability> capabilities;

  public CapabilityListUnsupportedCapabilityNotificationPacket(final List<Capability> capabilities) {
    this.capabilities = capabilities;
  }

  /* (non-Javadoc)
   * @see org.bgp4j.netty.protocol.NotificationPacket#encodeAdditionalPayload()
   */
  @Override
  protected ByteBuf encodeAdditionalPayload()
  {
    return CapabilityCodec.encodeCapabilities(this.capabilities);
  }

  /**
   * @return the capabilities
   */
  public List<Capability> getCapabilities() {
    return this.capabilities;
  }
}
