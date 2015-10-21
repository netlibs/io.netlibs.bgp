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
 * File: org.bgp4j.netty.protocol.open.CapabilityCodec.java
 */
package com.jive.oss.bgp.netty.protocol.open;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.jive.oss.bgp.net.AddressFamily;
import com.jive.oss.bgp.net.ORFSendReceive;
import com.jive.oss.bgp.net.ORFType;
import com.jive.oss.bgp.net.SubsequentAddressFamily;
import com.jive.oss.bgp.net.capabilities.AutonomousSystem4Capability;
import com.jive.oss.bgp.net.capabilities.Capability;
import com.jive.oss.bgp.net.capabilities.MultiProtocolCapability;
import com.jive.oss.bgp.net.capabilities.OutboundRouteFilteringCapability;
import com.jive.oss.bgp.net.capabilities.RouteRefreshCapability;
import com.jive.oss.bgp.net.capabilities.UnknownCapability;
import com.jive.oss.bgp.netty.BGPv4Constants;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class CapabilityCodec
{

  public static List<Capability> decodeCapabilities(final ByteBuf buffer)
  {
    final List<Capability> caps = new LinkedList<Capability>();

    while (buffer.isReadable())
    {
      caps.add(decodeCapability(buffer));
    }

    return caps;
  }

  public static Capability decodeCapability(final ByteBuf buffer)
  {
    Capability cap = null;

    try
    {
      buffer.markReaderIndex();

      final int type = buffer.readUnsignedByte();

      switch (type)
      {
        case BGPv4Constants.BGP_CAPABILITY_TYPE_MULTIPROTOCOL:
          cap = decodeMultiProtocolCapability(buffer);
          break;
        case BGPv4Constants.BGP_CAPABILITY_TYPE_ROUTE_REFRESH:
          cap = decodeRouteRefreshCapability(buffer);
          break;
        case BGPv4Constants.BGP_CAPABILITY_TYPE_AS4_NUMBERS:
          cap = decodeAutonomousSystem4Capability(buffer);
          break;
        case BGPv4Constants.BGP_CAPABILITY_TYPE_OUTBOUND_ROUTE_FILTERING:
          cap = decodeOutboundRouteFilteringCapability(buffer);
          break;
        default:
          cap = decodeUnknownCapability(type, buffer);
          break;
      }
    }
    catch (final CapabilityException e)
    {
      buffer.resetReaderIndex();

      final int type = buffer.readUnsignedByte();
      final int capLength = buffer.readUnsignedByte();

      final byte[] capPacket = new byte[capLength + 2];

      buffer.readBytes(capPacket, 2, capLength);
      capPacket[0] = (byte) type;
      capPacket[1] = (byte) capLength;

      e.setCapability(capPacket);
      throw e;
    }

    return cap;
  }

  private static Capability decodeUnknownCapability(final int type, final ByteBuf buffer)
  {
    final UnknownCapability cap = new UnknownCapability();

    cap.setCapabilityType(type);
    final int parameterLength = buffer.readUnsignedByte();

    if (parameterLength > 0)
    {
      final byte[] value = new byte[parameterLength];

      buffer.readBytes(value);
      cap.setValue(value);
    }

    return cap;
  }

  private static Capability decodeOutboundRouteFilteringCapability(final ByteBuf buffer)
  {
    final OutboundRouteFilteringCapability cap = new OutboundRouteFilteringCapability();

    assertMinimalLength(buffer, 5); // 2 octest AFI + 1 octet reserved + 1 octet SAFI + 1 octet number of (ORF type, Send/Receive) tuples

    cap.setAddressFamily(AddressFamily.fromCode(buffer.readUnsignedShort()));
    buffer.readByte();
    cap.setSubsequentAddressFamily(SubsequentAddressFamily.fromCode(buffer.readUnsignedByte()));

    final int orfs = buffer.readUnsignedByte();

    if (buffer.readableBytes() != (2 * orfs))
    {
      throw new UnspecificOpenPacketException("Expected " + (2 * orfs) + " octets parameter, got " + buffer.readableBytes() + " octets");
    }

    try
    {
      cap.getFilters().put(ORFType.fromCode(buffer.readUnsignedByte()), ORFSendReceive.fromCode(buffer.readUnsignedByte()));
    }
    catch (final IllegalArgumentException e)
    {
      throw new UnspecificOpenPacketException(e);
    }
    return cap;
  }

  private static Capability decodeAutonomousSystem4Capability(final ByteBuf buffer)
  {
    final AutonomousSystem4Capability cap = new AutonomousSystem4Capability();

    assertFixedLength(buffer, BGPv4Constants.BGP_CAPABILITY_LENGTH_AS4_NUMBERS);
    cap.setAutonomousSystem((int) buffer.readUnsignedInt());

    return cap;
  }

  private static Capability decodeRouteRefreshCapability(final ByteBuf buffer)
  {
    final RouteRefreshCapability cap = new RouteRefreshCapability();

    assertEmptyParameter(buffer);

    return cap;
  }

  private static Capability decodeMultiProtocolCapability(final ByteBuf buffer)
  {
    final MultiProtocolCapability cap = new MultiProtocolCapability();

    assertFixedLength(buffer, BGPv4Constants.BGP_CAPABILITY_LENGTH_MULTIPROTOCOL);

    cap.setAfi(AddressFamily.fromCode(buffer.readShort()));
    buffer.readByte(); // reserved
    cap.setSafi(SubsequentAddressFamily.fromCode(buffer.readByte()));

    return cap;
  }

  public static ByteBuf encodeCapabilities(final Collection<Capability> caps)
  {
    final ByteBuf buffer = Unpooled.buffer(BGPv4Constants.BGP_PACKET_MAX_LENGTH);

    if (caps != null)
    {
      for (final Capability cap : caps)
      {
        buffer.writeBytes(encodeCapability(cap));
      }
    }

    return buffer;
  }

  public static ByteBuf encodeCapability(final Capability cap)
  {
    final ByteBuf buffer = Unpooled.buffer(BGPv4Constants.BGP_CAPABILITY_HEADER_LENGTH + BGPv4Constants.BGP_CAPABILITY_MAX_VALUE_LENGTH);
    ByteBuf value = null;
    int capType = -1;

    if (cap instanceof MultiProtocolCapability)
    {
      value = encodeMultiprotocolCapability((MultiProtocolCapability) cap);
      capType = BGPv4Constants.BGP_CAPABILITY_TYPE_MULTIPROTOCOL;
    }
    else if (cap instanceof RouteRefreshCapability)
    {
      value = encodeRouteRefreshCapability((RouteRefreshCapability) cap);
      capType = BGPv4Constants.BGP_CAPABILITY_TYPE_ROUTE_REFRESH;
    }
    else if (cap instanceof AutonomousSystem4Capability)
    {
      value = encodeAutonomousSystem4Capability((AutonomousSystem4Capability) cap);
      capType = BGPv4Constants.BGP_CAPABILITY_TYPE_AS4_NUMBERS;
    }
    else if (cap instanceof OutboundRouteFilteringCapability)
    {
      value = encodeOutboundRouteFilteringCapability((OutboundRouteFilteringCapability) cap);
      capType = BGPv4Constants.BGP_CAPABILITY_TYPE_OUTBOUND_ROUTE_FILTERING;
    }
    else if (cap instanceof UnknownCapability)
    {
      value = encodeUnknownCapability((UnknownCapability) cap);
      capType = ((UnknownCapability) cap).getCapabilityType();
    }

    final int valueSize = (value != null) ? value.readableBytes() : 0;

    buffer.writeByte(capType);
    buffer.writeByte(valueSize);
    if (value != null)
    {
      buffer.writeBytes(value);
    }

    return buffer;

  }

  private static ByteBuf encodeUnknownCapability(final UnknownCapability cap)
  {
    ByteBuf buffer = null;

    if ((cap.getValue() != null) && (cap.getValue().length > 0))
    {
      buffer = Unpooled.buffer(cap.getValue().length);

      buffer.writeBytes(cap.getValue());
    }

    return buffer;
  }

  private static ByteBuf encodeOutboundRouteFilteringCapability(
      final OutboundRouteFilteringCapability cap)
  {
    final ByteBuf buffer = Unpooled.buffer(5 + (2 * cap.getFilters().size()));

    buffer.writeShort(cap.getAddressFamily().toCode());
    buffer.writeByte(0);
    buffer.writeByte(cap.getSubsequentAddressFamily().toCode());
    buffer.writeByte(cap.getFilters().size());

    for (final ORFType type : cap.getFilters().keySet())
    {
      buffer.writeByte(type.toCode());
      buffer.writeByte(cap.getFilters().get(type).toCode());
    }

    return buffer;
  }

  private static ByteBuf encodeAutonomousSystem4Capability(final AutonomousSystem4Capability cap)
  {
    final ByteBuf buffer = Unpooled.buffer(4);

    buffer.writeInt(cap.getAutonomousSystem());

    return buffer;
  }

  private static ByteBuf encodeRouteRefreshCapability(final RouteRefreshCapability cap)
  {
    return null;
  }

  private static ByteBuf encodeMultiprotocolCapability(
      final MultiProtocolCapability cap)
  {
    final ByteBuf buffer = Unpooled.buffer(4);

    if (cap.getAfi() != null)
    {
      buffer.writeShort(cap.getAfi().toCode());
    }
    else
    {
      buffer.writeShort(AddressFamily.RESERVED.toCode());
    }

    buffer.writeByte(0); // reserved

    if (cap.getSafi() != null)
    {
      buffer.writeByte(cap.getSafi().toCode());
    }
    else
    {
      buffer.writeByte(0);
    }

    return buffer;
  }

  private static final void assertEmptyParameter(final ByteBuf buffer)
  {
    final int parameterLength = buffer.readUnsignedByte();

    if (parameterLength != 0)
    {
      throw new UnspecificOpenPacketException("Expected zero-length parameter, got " + parameterLength + " octets");
    }
  }

  private static final void assertFixedLength(final ByteBuf buffer, final int length)
  {
    final int parameterLength = buffer.readUnsignedByte();

    if (parameterLength != length)
    {
      throw new UnspecificOpenPacketException("Expected " + length + " octets parameter, got " + parameterLength + " octets");
    }
  }

  private static final void assertMinimalLength(final ByteBuf buffer, final int length)
  {
    final int parameterLength = buffer.readUnsignedByte();

    if (parameterLength < length)
    {
      throw new UnspecificOpenPacketException("Expected " + length + " octets parameter, got " + parameterLength + " octets");
    }
  }

}
