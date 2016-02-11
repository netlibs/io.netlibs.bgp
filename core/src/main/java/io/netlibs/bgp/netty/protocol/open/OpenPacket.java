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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.netlibs.bgp.netty.protocol.BGPv4Packet;
import io.netlibs.bgp.netty.protocol.BGPv4PacketVisitor;
import io.netlibs.bgp.protocol.BGPv4Constants;
import io.netlibs.bgp.protocol.capabilities.Capability;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@AllArgsConstructor
@Builder
public class OpenPacket extends BGPv4Packet
{

  private int protocolVersion;
  private int autonomousSystem;
  private int holdTime;
  private long bgpIdentifier;
  
  @Singular
  private List<Capability> capabilities = new LinkedList<Capability>();

  public OpenPacket()
  {
  }

  public OpenPacket(final int protocolVersion)
  {
    this.protocolVersion = protocolVersion;
  }

  public OpenPacket(final int protocolVersion, final int autonomousSysten, final long bgpIdentifier)
  {
    this(protocolVersion);

    this.autonomousSystem = autonomousSysten;
    this.bgpIdentifier = bgpIdentifier;
  }

  public OpenPacket(final int protocolVersion, final int autonomousSysten, final long bgpIdentifier, final int holdTime)
  {
    this(protocolVersion, autonomousSysten, bgpIdentifier);
    this.holdTime = holdTime;
  }

  public OpenPacket(final int protocolVersion, final int autonomousSysten, final long bgpIdentifier, final int holdTime, final Collection<Capability> capabilities)
  {
    this(protocolVersion, autonomousSysten, bgpIdentifier, holdTime);
    this.capabilities.addAll(capabilities);
  }

  public OpenPacket(final int protocolVersion, final int autonomousSysten, final long bgpIdentifier, final int holdTime, final Capability[] capabilities)
  {

    this(protocolVersion, autonomousSysten, bgpIdentifier, holdTime);

    for (final Capability cap : capabilities)
    {
      this.capabilities.add(cap);
    }
  }

  /**
   * @return the protocolVersion
   */
  public int getProtocolVersion()
  {
    return this.protocolVersion;
  }

  /**
   * @param protocolVersion
   *          the protocolVersion to set
   */
  public void setProtocolVersion(final int protocolVersion)
  {
    this.protocolVersion = protocolVersion;
  }

  /**
   * @return the autonomuosSystem
   */
  public int getAutonomousSystem()
  {
    return this.autonomousSystem;
  }

  /**
   * @param autonomuosSystem
   *          the autonomuosSystem to set
   */
  public void setAutonomousSystem(final int autonomuosSystem)
  {
    this.autonomousSystem = autonomuosSystem;
  }

  /**
   * @return the holdTime
   */
  public int getHoldTime()
  {
    return this.holdTime;
  }

  /**
   * @param holdTime
   *          the holdTime to set
   */
  public void setHoldTime(final int holdTime)
  {
    this.holdTime = holdTime;
  }

  /**
   * @return the bgpIdentifier
   */
  public long getBgpIdentifier()
  {
    return this.bgpIdentifier;
  }

  /**
   * @param bgpIdentifier
   *          the bgpIdentifier to set
   */
  public void setBgpIdentifier(final long bgpIdentifier)
  {
    this.bgpIdentifier = bgpIdentifier;
  }

  /**
   * @return the capabilities
   */
  public List<Capability> getCapabilities()
  {
    return this.capabilities;
  }

  /**
   * @param capabilities
   *          the capabilities to set
   */
  public void setCapabilities(final List<Capability> capabilities)
  {
    this.capabilities = capabilities;
  }

  @Override
  protected ByteBuf encodePayload()
  {
    final ByteBuf buffer = Unpooled.buffer(BGPv4Constants.BGP_PACKET_MAX_LENGTH);

    buffer.writeByte(this.getProtocolVersion());
    buffer.writeShort(this.getAutonomousSystem());
    buffer.writeShort(this.getHoldTime());
    buffer.writeInt((int) this.getBgpIdentifier());

    final ByteBuf capabilities = CapabilityCodec.encodeCapabilities(this.getCapabilities());

    if (capabilities.readableBytes() > 0)
    {
      buffer.writeByte(capabilities.readableBytes() + 2); // cap length + type byte + parameter length byte
      buffer.writeByte(BGPv4Constants.BGP_OPEN_PARAMETER_TYPE_CAPABILITY); // type byte
      buffer.writeByte(capabilities.readableBytes()); // parameter length
      buffer.writeBytes(capabilities);
    }
    else
    {
      buffer.writeByte(0); // no capabilites encoded --> optional parameter length equals 0
    }
    return buffer;
  }

  @Override
  public int getType()
  {
    return BGPv4Constants.BGP_PACKET_TYPE_OPEN;
  }

  /**
   * look up a specific capability in the list of provided capabilities
   *
   * @param clazzToFind
   *          the class of the capability to find
   * @return the capability or null if the capability is not passed along in the OPEN packet
   */
  @SuppressWarnings("unchecked")
  public <T extends Capability> T findCapability(final Class<T> clazzToFind)
  {
    T cap = null;

    if (this.capabilities != null)
    {
      for (final Capability c : this.capabilities)
      {
        if (c.getClass().equals(clazzToFind))
        {
          cap = (T) c;
          break;
        }
      }
    }
    return cap;
  }

  @Override
  public <T> T apply(final BGPv4PacketVisitor<T> visitor)
  {
    return visitor.visit(this);
  }

  @Override
  public String toString()
  {
    final ToStringBuilder builder = (new ToStringBuilder(this))
        .append("type", this.getType())
        .append("autonomousSystem", this.autonomousSystem)
        .append("bgpIdentifier", this.bgpIdentifier)
        .append("holdTime", this.holdTime)
        .append("protocolVersion", this.protocolVersion);

    for (final Capability cap : this.capabilities)
    {
      builder.append("capability", cap);
    }

    return builder.toString();
  }
}
