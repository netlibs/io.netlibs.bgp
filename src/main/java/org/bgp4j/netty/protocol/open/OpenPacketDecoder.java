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
 * File: org.bgp4j.netty.protocol.open.OpenPacketDecoder.java
 */
package org.bgp4j.netty.protocol.open;

import org.bgp4j.netty.BGPv4Constants;
import org.bgp4j.netty.protocol.NotificationPacket;
import org.bgp4j.netty.protocol.ProtocolPacketUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class OpenPacketDecoder
{

  /**
   * decode the OPEN network packet. The passed channel buffer MUST point to the first packet octet AFTER the type octet.
   *
   * @param buffer
   *          the buffer containing the data.
   * @return
   */
  public NotificationPacket decodeOpenNotificationPacket(final ByteBuf buffer, final int errorSubcode)
  {
    NotificationPacket packet = null;

    switch (errorSubcode)
    {
      case OpenNotificationPacket.SUBCODE_BAD_BGP_IDENTIFIER:
        packet = new BadBgpIdentifierNotificationPacket();
        break;
      case OpenNotificationPacket.SUBCODE_BAD_PEER_AS:
        packet = new BadPeerASNotificationPacket();
        break;
      case OpenNotificationPacket.SUBCODE_UNACCEPTABLE_HOLD_TIMER:
        packet = new UnacceptableHoldTimerNotificationPacket();
        break;
      case OpenNotificationPacket.SUBCODE_UNSPECIFIC:
        packet = new UnspecificOpenNotificationPacket();
        break;
      case OpenNotificationPacket.SUBCODE_UNSUPPORTED_OPTIONAL_PARAMETER:
        packet = new UnsupportedOptionalParameterNotificationPacket();
        break;
      case OpenNotificationPacket.SUBCODE_UNSUPPORTED_VERSION_NUMBER:
        packet = new UnsupportedVersionNumberNotificationPacket(buffer.readUnsignedShort());
        break;
      case OpenNotificationPacket.SUBCODE_UNSUPPORTED_CAPABILITY:
        packet = new CapabilityListUnsupportedCapabilityNotificationPacket(CapabilityCodec.decodeCapabilities(buffer));
        break;
    }

    return packet;
  }

  private static long IPV4_MULTICAST_MASK = 0xe0000000L;

  /**
   * decode the OPEN network packet. The passed channel buffer MUST point to the first packet octet AFTER the packet type and the buffer
   * must be at least 9 octets large at this point.
   *
   * @param buffer
   *          the buffer containing the data.
   * @return
   */
  public OpenPacket decodeOpenPacket(final ByteBuf buffer)
  {
    final OpenPacket packet = new OpenPacket();

    ProtocolPacketUtils.verifyPacketSize(buffer, BGPv4Constants.BGP_PACKET_MIN_SIZE_OPEN, -1);

    packet.setProtocolVersion(buffer.readUnsignedByte());
    if (packet.getProtocolVersion() != BGPv4Constants.BGP_VERSION)
    {
      throw new UnsupportedVersionNumberException(BGPv4Constants.BGP_VERSION);
    }
    packet.setAutonomousSystem(buffer.readUnsignedShort());
    packet.setHoldTime(buffer.readUnsignedShort());
    packet.setBgpIdentifier(buffer.readUnsignedInt());
    if ((packet.getBgpIdentifier() & IPV4_MULTICAST_MASK) == IPV4_MULTICAST_MASK)
    {
      throw new BadBgpIdentifierException();
    }

    final int parameterLength = buffer.readUnsignedByte();

    if (parameterLength > 0)
    {
      while (buffer.isReadable())
      {
        final int parameterType = buffer.readUnsignedByte();
        final int paramLength = buffer.readUnsignedByte();

        final ByteBuf valueBuffer = Unpooled.buffer(paramLength);

        buffer.readBytes(valueBuffer);

        switch (parameterType)
        {
          case BGPv4Constants.BGP_OPEN_PARAMETER_TYPE_AUTH:
            break;
          case BGPv4Constants.BGP_OPEN_PARAMETER_TYPE_CAPABILITY:
            packet.getCapabilities().addAll(CapabilityCodec.decodeCapabilities(valueBuffer));
            break;
          default:
            throw new UnsupportedOptionalParameterException();
        }
      }
    }

    return packet;
  }

}
