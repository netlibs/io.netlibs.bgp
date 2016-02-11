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
 * File: org.bgp4j.netty.protocol.CapabilityException.java
 */
package io.netlibs.bgp.netty.protocol.open;

import java.util.Collection;

import io.netlibs.bgp.protocol.BGPv4Constants;
import io.netlibs.bgp.protocol.capabilities.Capability;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public abstract class CapabilityException extends OpenPacketException
{

  /**
   *
   */
  private static final long serialVersionUID = -5564369816036195511L;

  private byte[] capability;

  protected CapabilityException()
  {
    super();
  }

  protected CapabilityException(final byte[] capability)
  {
    this.capability = capability;
  }

  protected CapabilityException(final Capability cap)
  {
    this.capability = this.encondeCapability(cap);
  }

  protected CapabilityException(final Collection<Capability> caps)
  {
    this.capability = this.encondeCapabilities(caps);
  }

  protected CapabilityException(final String message, final byte[] capability)
  {
    super(message);

    this.capability = capability;
  }

  protected CapabilityException(final String message, final Capability cap)
  {
    super(message);

    this.capability = this.encondeCapability(cap);
  }

  protected CapabilityException(final String message, final Collection<Capability> caps)
  {
    super(message);

    this.capability = this.encondeCapabilities(caps);
  }

  /**
   * @return the capability
   */
  public byte[] getCapability()
  {
    return this.capability;
  }

  /**
   * @param capability
   *          the capability to set
   */
  public void setCapability(final byte[] capability)
  {
    this.capability = capability;
  }

  private byte[] encondeCapability(final Capability cap)
  {
    final ByteBuf buffer = CapabilityCodec.encodeCapability(cap);
    final byte[] packet = new byte[buffer.readableBytes()];

    buffer.readBytes(this.capability);

    return packet;
  }

  private byte[] encondeCapabilities(final Collection<Capability> caps)
  {
    final ByteBuf buffer = Unpooled.buffer(BGPv4Constants.BGP_PACKET_MAX_LENGTH);

    for (final Capability cap : caps)
    {
      buffer.writeBytes(CapabilityCodec.encodeCapability(cap));
    }

    final byte[] packet = new byte[buffer.readableBytes()];

    buffer.readBytes(this.capability);

    return packet;
  }
}
