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
 * File: org.bgp4j.netty.protocol.refresh.RouteRefreshPacketDecoder.java 
 */
package io.netlibs.bgp.netty.codec;

import java.util.LinkedList;
import java.util.List;

import io.netlibs.bgp.net.AddressFamily;
import io.netlibs.bgp.net.AddressPrefixBasedORFEntry;
import io.netlibs.bgp.net.ORFAction;
import io.netlibs.bgp.net.ORFEntry;
import io.netlibs.bgp.net.ORFMatch;
import io.netlibs.bgp.net.ORFRefreshType;
import io.netlibs.bgp.net.ORFType;
import io.netlibs.bgp.net.OutboundRouteFilter;
import io.netlibs.bgp.net.SubsequentAddressFamily;
import io.netlibs.bgp.netty.NLRICodec;
import io.netlibs.bgp.netty.protocol.BGPv4Packet;
import io.netlibs.bgp.netty.protocol.refresh.RouteRefreshPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@Slf4j
public class RouteRefreshPacketDecoder
{

  /**
   * decode the REFRESH network packet. The passed channel buffer MUST point to the first packet octet AFTER the type octet.
   * 
   * @param buffer
   *          the buffer containing the data.
   * 
   * @return the decoded packet or null on decoding problems. Neither RFC2918 nor RFC5291 nor RFC4271 describe an error handling procedure,
   *         so best advise is to ignore invalid packets for now.
   */

  public BGPv4Packet decodeRouteRefreshPacket(ByteBuf buffer)
  {

    RouteRefreshPacket packet = null;

    try
    {
      AddressFamily af = AddressFamily.fromCode(buffer
          .readUnsignedShort());

      buffer.readByte(); // swallow reserved octet

      SubsequentAddressFamily saf = SubsequentAddressFamily
          .fromCode(buffer.readUnsignedByte());

      packet = new RouteRefreshPacket(af, saf);

      if (buffer.isReadable())
      {

        // we have outbound router filter rules here
        OutboundRouteFilter orf = new OutboundRouteFilter(af, saf);

        orf.setRefreshType(ORFRefreshType.fromCode(buffer.readUnsignedByte()));

        while (buffer.isReadable())
        {
          ORFType orfType = ORFType.fromCode(buffer.readUnsignedByte());
          ByteBuf entriesBuffer = Unpooled.buffer(buffer.readUnsignedShort());

          buffer.readBytes(entriesBuffer);
          orf.addAllORFEntries(decodeORFEntries(entriesBuffer, orfType));
        }

        packet.setOutboundRouteFilter(orf);

      }

    }
    catch (Exception e)
    {
      log.error("cannot decode ROUTE_REFRESH packet, suppressing it from further processing", e);
      packet = null;
    }

    return packet;
  }

  private List<ORFEntry> decodeORFEntries(ByteBuf buffer, ORFType orfType)
  {
    List<ORFEntry> entries = new LinkedList<ORFEntry>();

    while (buffer.isReadable())
    {

      int actionMatch = buffer.readUnsignedByte();

      ORFAction action = ORFAction.fromCode((actionMatch >> 6) & 0x03);
      ORFMatch match = ORFMatch.fromCode((actionMatch >> 5) & 0x01);

      switch (orfType)
      {
        case ADDRESS_PREFIX_BASED:
          entries.add(decodeAddressPrefixBasedORFEntry(buffer, action, match));
          break;
        default:
          throw new IllegalArgumentException("cannot decode OutboudRouteFilter entries of type " + orfType);
      }

    }

    return entries;
  }

  private ORFEntry decodeAddressPrefixBasedORFEntry(ByteBuf buffer, ORFAction action, ORFMatch match)
  {

    AddressPrefixBasedORFEntry entry = new AddressPrefixBasedORFEntry(action, match);

    if (action != ORFAction.REMOVE_ALL)
    {
      entry.setSequence((int) buffer.readUnsignedInt());
      entry.setMinLength(buffer.readUnsignedByte());
      entry.setMaxLength(buffer.readUnsignedByte());
      entry.setPrefix(NLRICodec.decodeNLRI(buffer));
    }

    return entry;

  }

}
