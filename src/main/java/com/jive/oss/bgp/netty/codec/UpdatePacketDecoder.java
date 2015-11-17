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
 * File: org.bgp4j.netty.protocol.update.UpdatePacketDecoder.java
 */
package com.jive.oss.bgp.netty.codec;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import com.google.common.net.InetAddresses;
import com.jive.oss.bgp.net.ASType;
import com.jive.oss.bgp.net.AddressFamily;
import com.jive.oss.bgp.net.NetworkLayerReachabilityInformation;
import com.jive.oss.bgp.net.NonTransitiveExtendedCommunityType;
import com.jive.oss.bgp.net.PathSegment;
import com.jive.oss.bgp.net.SubsequentAddressFamily;
import com.jive.oss.bgp.net.TransitiveExtendedCommunityType;
import com.jive.oss.bgp.net.TransitiveIPv4AddressSpecificExtCommSubTypes;
import com.jive.oss.bgp.net.TransitiveIPv4AddressTwoByteAdministratorRT;
import com.jive.oss.bgp.net.TransitiveTwoByteASNFourByteAdministratorRT;
import com.jive.oss.bgp.net.TransitiveTwoOctetASSpecificExtCommSubTypes;
import com.jive.oss.bgp.net.UnknownNonTransitiveExtendedCommunity;
import com.jive.oss.bgp.net.UnknownTransitiveExtendedCommunity;
import com.jive.oss.bgp.net.UnknownTransitiveIPv4AddressSpecificExtendedCommunity;
import com.jive.oss.bgp.net.UnknownTransitiveTwoByteASNSpecificExtendedCommunity;
import com.jive.oss.bgp.net.attributes.ASPathAttribute;
import com.jive.oss.bgp.net.attributes.AbstractExtendedCommunityInterface;
import com.jive.oss.bgp.net.attributes.AggregatorPathAttribute;
import com.jive.oss.bgp.net.attributes.AtomicAggregatePathAttribute;
import com.jive.oss.bgp.net.attributes.ClusterListPathAttribute;
import com.jive.oss.bgp.net.attributes.CommunityMember;
import com.jive.oss.bgp.net.attributes.CommunityPathAttribute;
import com.jive.oss.bgp.net.attributes.ExtendedCommunityPathAttribute;
import com.jive.oss.bgp.net.attributes.LocalPrefPathAttribute;
import com.jive.oss.bgp.net.attributes.MultiExitDiscPathAttribute;
import com.jive.oss.bgp.net.attributes.MultiProtocolReachableNLRI;
import com.jive.oss.bgp.net.attributes.MultiProtocolUnreachableNLRI;
import com.jive.oss.bgp.net.attributes.NextHopPathAttribute;
import com.jive.oss.bgp.net.attributes.OriginPathAttribute;
import com.jive.oss.bgp.net.attributes.OriginatorIDPathAttribute;
import com.jive.oss.bgp.net.attributes.PathAttribute;
import com.jive.oss.bgp.net.attributes.UnknownPathAttribute;
import com.jive.oss.bgp.netty.BGPv4Constants;
import com.jive.oss.bgp.netty.NLRICodec;
import com.jive.oss.bgp.netty.protocol.BGPv4Packet;
import com.jive.oss.bgp.netty.protocol.NotificationPacket;
import com.jive.oss.bgp.netty.protocol.update.AttributeException;
import com.jive.oss.bgp.netty.protocol.update.AttributeFlagsNotificationPacket;
import com.jive.oss.bgp.netty.protocol.update.AttributeLengthException;
import com.jive.oss.bgp.netty.protocol.update.AttributeLengthNotificationPacket;
import com.jive.oss.bgp.netty.protocol.update.InvalidNetworkFieldException;
import com.jive.oss.bgp.netty.protocol.update.InvalidNetworkFieldNotificationPacket;
import com.jive.oss.bgp.netty.protocol.update.InvalidNextHopException;
import com.jive.oss.bgp.netty.protocol.update.InvalidNextHopNotificationPacket;
import com.jive.oss.bgp.netty.protocol.update.InvalidOriginException;
import com.jive.oss.bgp.netty.protocol.update.InvalidOriginNotificationPacket;
import com.jive.oss.bgp.netty.protocol.update.MalformedASPathAttributeException;
import com.jive.oss.bgp.netty.protocol.update.MalformedASPathAttributeNotificationPacket;
import com.jive.oss.bgp.netty.protocol.update.MalformedAttributeListException;
import com.jive.oss.bgp.netty.protocol.update.MalformedAttributeListNotificationPacket;
import com.jive.oss.bgp.netty.protocol.update.MissingWellKnownAttributeNotificationPacket;
import com.jive.oss.bgp.netty.protocol.update.OptionalAttributeErrorException;
import com.jive.oss.bgp.netty.protocol.update.OptionalAttributeErrorNotificationPacket;
import com.jive.oss.bgp.netty.protocol.update.OriginCodec;
import com.jive.oss.bgp.netty.protocol.update.PathAttributeCodec;
import com.jive.oss.bgp.netty.protocol.update.PathSegmentTypeCodec;
import com.jive.oss.bgp.netty.protocol.update.UnrecognizedWellKnownAttributeNotificationPacket;
import com.jive.oss.bgp.netty.protocol.update.UpdateNotificationPacket;
import com.jive.oss.bgp.netty.protocol.update.UpdatePacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

public class UpdatePacketDecoder
{

  /**
   * decode the UPDATE network packet. The passed channel buffer MUST point to the first packet octet AFTER the type octet.
   *
   * @param buffer
   *          the buffer containing the data.
   * @return
   */

  public BGPv4Packet decodeUpdatePacket(final ByteBuf buffer)
  {

    final UpdatePacket packet = new UpdatePacket();

    ProtocolPacketUtils.verifyPacketSize(buffer, BGPv4Constants.BGP_PACKET_MIN_SIZE_UPDATE, -1);

    if (buffer.readableBytes() < 2)
    {
      throw new MalformedAttributeListException();
    }

    // handle withdrawn routes
    final int withdrawnOctets = buffer.readUnsignedShort();

    // sanity checking
    if (withdrawnOctets > buffer.readableBytes())
    {
      throw new MalformedAttributeListException();
    }

    ByteBuf withdrawnBuffer = null;

    if (withdrawnOctets > 0)
    {
      withdrawnBuffer = Unpooled.buffer(withdrawnOctets);

      buffer.readBytes(withdrawnBuffer);
    }

    // sanity checking
    if (buffer.readableBytes() < 2)
    {
      throw new MalformedAttributeListException();
    }

    // handle path attributes
    final int pathAttributeOctets = buffer.readUnsignedShort();

    // sanity checking
    if (pathAttributeOctets > buffer.readableBytes())
    {
      throw new MalformedAttributeListException();
    }

    ByteBuf pathAttributesBuffer = null;

    if (pathAttributeOctets > 0)
    {
      pathAttributesBuffer = Unpooled.buffer(pathAttributeOctets);

      buffer.readBytes(pathAttributesBuffer);
    }

    if (withdrawnBuffer != null)
    {
      try
      {
        packet.getWithdrawnRoutes().addAll(this.decodeWithdrawnRoutes(withdrawnBuffer));
      }
      catch (final IndexOutOfBoundsException e)
      {
        throw new MalformedAttributeListException();
      }
    }

    if (pathAttributesBuffer != null)
    {
      try
      {
        packet.getPathAttributes().addAll(this.decodePathAttributes(pathAttributesBuffer));
      }
      catch (final IndexOutOfBoundsException ex)
      {
        throw new MalformedAttributeListException();
      }
    }

    // handle network layer reachability information
    if (buffer.readableBytes() > 0)
    {
      try
      {
        while (buffer.readableBytes() > 0)
        {
          packet.getNlris().add(NLRICodec.decodeNLRI(buffer));
        }
      }
      catch (final IndexOutOfBoundsException e)
      {
        throw new InvalidNetworkFieldException();
      }
      catch (final IllegalArgumentException e)
      {
        throw new InvalidNetworkFieldException();
      }
    }

    return packet;
  }

  /**
   * decode a NOTIFICATION packet that corresponds to UPDATE apckets. The passed channel buffer MUST point to the first packet octet AFTER
   * the terror sub code.
   *
   * @param buffer
   *          the buffer containing the data.
   * @return
   */
  public NotificationPacket decodeUpdateNotification(final ByteBuf buffer, final int errorSubcode)
  {
    UpdateNotificationPacket packet = null;
    byte[] offendingAttribute = null;

    if (buffer.isReadable())
    {
      offendingAttribute = new byte[buffer.readableBytes()];

      buffer.readBytes(offendingAttribute);
    }

    switch (errorSubcode)
    {
      case UpdateNotificationPacket.SUBCODE_MALFORMED_ATTRIBUTE_LIST:
        packet = new MalformedAttributeListNotificationPacket();
        break;
      case UpdateNotificationPacket.SUBCODE_UNRECOGNIZED_WELL_KNOWN_ATTRIBUTE:
        packet = new UnrecognizedWellKnownAttributeNotificationPacket(offendingAttribute);
        break;
      case UpdateNotificationPacket.SUBCODE_MISSING_WELL_KNOWN_ATTRIBUTE:
        packet = new MissingWellKnownAttributeNotificationPacket(0);
        break;
      case UpdateNotificationPacket.SUBCODE_ATTRIBUTE_FLAGS_ERROR:
        packet = new AttributeFlagsNotificationPacket(offendingAttribute);
        break;
      case UpdateNotificationPacket.SUBCODE_ATTRIBUTE_LENGTH_ERROR:
        packet = new AttributeLengthNotificationPacket(offendingAttribute);
        break;
      case UpdateNotificationPacket.SUBCODE_INVALID_ORIGIN_ATTRIBUTE:
        packet = new InvalidOriginNotificationPacket(offendingAttribute);
        break;
      case UpdateNotificationPacket.SUBCODE_INVALID_NEXT_HOP_ATTRIBUTE:
        packet = new InvalidNextHopNotificationPacket(offendingAttribute);
        break;
      case UpdateNotificationPacket.SUBCODE_OPTIONAL_ATTRIBUTE_ERROR:
        packet = new OptionalAttributeErrorNotificationPacket(offendingAttribute);
        break;
      case UpdateNotificationPacket.SUBCODE_INVALID_NETWORK_FIELD:
        packet = new InvalidNetworkFieldNotificationPacket();
        break;
      case UpdateNotificationPacket.SUBCODE_MALFORMED_AS_PATH:
        packet = new MalformedASPathAttributeNotificationPacket(offendingAttribute);
        break;
    }

    return packet;
  }

  private ASPathAttribute decodeASPathAttribute(final ByteBuf buffer, final ASType asType)
  {
    final ASPathAttribute attr = new ASPathAttribute(asType);

    while (buffer.isReadable())
    {
      if (buffer.readableBytes() < 2)
      {
        throw new MalformedASPathAttributeException();
      }

      final int segmentType = buffer.readUnsignedByte();
      final int pathLength = buffer.readUnsignedByte();
      final int pathOctetLength = (pathLength * (asType == ASType.AS_NUMBER_4OCTETS ? 4 : 2));

      if (buffer.readableBytes() < pathOctetLength)
      {
        throw new MalformedASPathAttributeException();
      }

      final PathSegment segment = new PathSegment(asType);

      try
      {
        segment.setPathSegmentType(PathSegmentTypeCodec.fromCode(segmentType));
      }
      catch (final IllegalArgumentException e)
      {
        throw new MalformedASPathAttributeException();
      }

      for (int i = 0; i < pathLength; i++)
      {
        if (asType == ASType.AS_NUMBER_4OCTETS)
        {
          segment.getAses().add((int) buffer.readUnsignedInt());
        }
        else
        {
          segment.getAses().add(buffer.readUnsignedShort());
        }
      }

      attr.getPathSegments().add(segment);
    }

    return attr;
  }

  private OriginPathAttribute decodeOriginPathAttribute(final ByteBuf buffer)
  {
    final OriginPathAttribute attr = new OriginPathAttribute();

    if (buffer.readableBytes() != 1)
    {
      throw new AttributeLengthException();
    }

    try
    {
      attr.setOrigin(OriginCodec.fromCode(buffer.readUnsignedByte()));
    }
    catch (final IllegalArgumentException e)
    {
      throw new InvalidOriginException();
    }

    return attr;
  }

  private MultiExitDiscPathAttribute decodeMultiExitDiscPathAttribute(final ByteBuf buffer)
  {
    final MultiExitDiscPathAttribute attr = new MultiExitDiscPathAttribute();

    if (buffer.readableBytes() != 4)
    {
      throw new AttributeLengthException();
    }

    attr.setDiscriminator((int) buffer.readUnsignedInt());

    return attr;
  }

  private LocalPrefPathAttribute decodeLocalPrefPathAttribute(final ByteBuf buffer)
  {
    final LocalPrefPathAttribute attr = new LocalPrefPathAttribute();

    if (buffer.readableBytes() != 4)
    {
      throw new AttributeLengthException();
    }

    attr.setLocalPreference((int) buffer.readUnsignedInt());

    return attr;
  }

  private NextHopPathAttribute decodeNextHopPathAttribute(final ByteBuf buffer)
  {
    final NextHopPathAttribute attr = new NextHopPathAttribute();

    if (buffer.readableBytes() != 4)
    {
      throw new AttributeLengthException();
    }

    try
    {
      final byte[] addr = new byte[4];

      buffer.readBytes(addr);
      attr.setNextHop((Inet4Address) InetAddress.getByAddress(addr));
    }
    catch (final IllegalArgumentException e)
    {
      throw new InvalidNextHopException();
    }
    catch (final UnknownHostException e)
    {
      throw new InvalidNextHopException();
    }

    return attr;
  }

  private AtomicAggregatePathAttribute decodeAtomicAggregatePathAttribute(final ByteBuf buffer)
  {
    final AtomicAggregatePathAttribute attr = new AtomicAggregatePathAttribute();

    if (buffer.readableBytes() != 0)
    {
      throw new AttributeLengthException();
    }

    return attr;
  }

  private AggregatorPathAttribute decodeAggregatorPathAttribute(final ByteBuf buffer, final ASType asType)
  {
    final AggregatorPathAttribute attr = new AggregatorPathAttribute(asType);

    if (buffer.readableBytes() != PathAttributeCodec.valueLength(attr))
    {
      throw new AttributeLengthException();
    }

    if (asType == ASType.AS_NUMBER_4OCTETS)
    {
      attr.setAsNumber((int) buffer.readUnsignedInt());
    }
    else
    {
      attr.setAsNumber(buffer.readUnsignedShort());
    }

    try
    {
      final byte[] addr = new byte[4];

      buffer.readBytes(addr);
      attr.setAggregator((Inet4Address) InetAddress.getByAddress(addr));
    }
    catch (final UnknownHostException e)
    {
      throw new OptionalAttributeErrorException();
    }

    return attr;
  }

  private CommunityPathAttribute decodeCommunityPathAttribute(final ByteBuf buffer)
  {
    final CommunityPathAttribute attr = new CommunityPathAttribute();

    if ((buffer.readableBytes() < 4) || ((buffer.readableBytes() % 4) != 0))
    {
      throw new OptionalAttributeErrorException();
    }

    while (buffer.isReadable())
    {
      final CommunityMember member = new CommunityMember();

      member.setAsNumber(buffer.readUnsignedShort());
      member.setValue(buffer.readUnsignedShort());
      System.err.printf("adding %d:%d to members\n", member.getAsNumber(), member.getValue());

      attr.getMembers().add(member);
    }

    return attr;
  }

  private ExtendedCommunityPathAttribute decodeExtendedCommunityPathAttribute(final ByteBuf buffer){
    final ExtendedCommunityPathAttribute attr = new ExtendedCommunityPathAttribute();
    
    if ((buffer.readableBytes() < 8) || ((buffer.readableBytes() % 8) != 0))
    {
      throw new OptionalAttributeErrorException();
    }
    
    while(buffer.isReadable())
    {
      AbstractExtendedCommunityInterface extcomm;
      // we need to check whether this is a transitive or non-transitive value
      // and then determine whether we need to red the lower type byte
      byte higherType = buffer.readByte();
      byte higherTypeCompare = (byte) (higherType & ~(1 << 6));
      if (((higherType >> 6) & 1) == 0)
      {
        // bit 7 is not set in the byte, therefore this is a transitive type
        // clear bit 8, not interested in this value
        TransitiveExtendedCommunityType transCommType = TransitiveExtendedCommunityType.fromCode((byte) (higherType & (~(3 << 6))));
       
        switch(transCommType)
        {
          case TWO_OCTET_AS_SPECIFIC:
            TransitiveTwoOctetASSpecificExtCommSubTypes twoOctASNLowerType = TransitiveTwoOctetASSpecificExtCommSubTypes.fromCode(buffer.readByte());
            switch(twoOctASNLowerType){
              case ROUTE_TARGET:
                extcomm = new TransitiveTwoByteASNFourByteAdministratorRT((int) buffer.readShort(), (long) buffer.readInt());
                break;
              default:
                // all non-RT types are currently unimplemented
                extcomm = new UnknownTransitiveTwoByteASNSpecificExtendedCommunity(transCommType, twoOctASNLowerType, buffer.readBytes(6).array());
            }
            break;
          case TWO_OCTET_IPv4_ADDRESS_SPECIFIC:
            TransitiveIPv4AddressSpecificExtCommSubTypes ipv4LowerType = TransitiveIPv4AddressSpecificExtCommSubTypes.fromCode(buffer.readByte());
            switch (ipv4LowerType){
              case ROUTE_TARGET:
                try
                {
                  extcomm = new TransitiveIPv4AddressTwoByteAdministratorRT((Inet4Address) InetAddresses.fromLittleEndianByteArray(buffer.readBytes(4).array()), (int) buffer.readShort());
                }
                catch (UnknownHostException e)
                {
                  ByteBuf data = Unpooled.buffer();
                  data.getByte(ipv4LowerType.toCode());
                  data.readBytes(buffer.readBytes(6));
                  extcomm = new UnknownTransitiveExtendedCommunity(transCommType,data.array());
                }
              default:
                // all non-RT types are currently unimplemented
                extcomm = new UnknownTransitiveIPv4AddressSpecificExtendedCommunity(transCommType, ipv4LowerType, buffer.readBytes(6).array());
            }
            break;
          default:
            // by default, just create an unknown type, reading the subsequent
            // 7 bytes (we have already read byte 1)
            extcomm = new UnknownTransitiveExtendedCommunity(transCommType, buffer.readBytes(7).array());
        }
      } else {
        // bit 7 is set, these are non-transitive        
        NonTransitiveExtendedCommunityType nonTransCommType = NonTransitiveExtendedCommunityType.fromCode((byte) (higherType & (~(3 << 6))));
        // all non-transitive types are currently unimplemented
        extcomm = new UnknownNonTransitiveExtendedCommunity(nonTransCommType, buffer.readBytes(7).array()); 
      }
      attr.getMembers().add(extcomm);
    }
    return attr;
  }
  
  private MultiProtocolReachableNLRI decodeMpReachNlriPathAttribute(final ByteBuf buffer)
  {
    final MultiProtocolReachableNLRI attr = new MultiProtocolReachableNLRI();

    try
    {
      attr.setAddressFamily(AddressFamily.fromCode(buffer.readUnsignedShort()));
      attr.setSubsequentAddressFamily(SubsequentAddressFamily.fromCode(buffer.readUnsignedByte()));

      final int nextHopLength = buffer.readUnsignedByte();

      if (nextHopLength > 0)
      {
        final byte[] nextHop = new byte[nextHopLength];

        buffer.readBytes(nextHop);
        attr.setNextHopAddress(nextHop);
      }

      buffer.readByte(); // reserved

      while (buffer.isReadable())
      {
        attr.getNlris().add(NLRICodec.decodeNLRI(buffer));
      }
    }
    catch (final RuntimeException e)
    {
      throw new OptionalAttributeErrorException();
    }

    return attr;
  }

  private MultiProtocolUnreachableNLRI decodeMpUnreachNlriPathAttribute(final ByteBuf buffer)
  {
    final MultiProtocolUnreachableNLRI attr = new MultiProtocolUnreachableNLRI();

    try
    {
      attr.setAddressFamily(AddressFamily.fromCode(buffer.readUnsignedShort()));
      attr.setSubsequentAddressFamily(SubsequentAddressFamily.fromCode(buffer.readUnsignedByte()));

      while (buffer.isReadable())
      {
        attr.getNlris().add(NLRICodec.decodeNLRI(buffer));
      }
    }
    catch (final RuntimeException e)
    {
      throw new OptionalAttributeErrorException();
    }

    return attr;
  }

  private OriginatorIDPathAttribute decodeOriginatorIDPathAttribute(final ByteBuf buffer)
  {
    final OriginatorIDPathAttribute attr = new OriginatorIDPathAttribute();

    try
    {
      attr.setOriginatorID((int) buffer.readUnsignedInt());
    }
    catch (final RuntimeException e)
    {
      throw new OptionalAttributeErrorException();
    }

    return attr;
  }

  private ClusterListPathAttribute decodeClusterListPathAttribute(final ByteBuf buffer)
  {
    final ClusterListPathAttribute attr = new ClusterListPathAttribute();

    try
    {
      while (buffer.isReadable())
      {
        attr.getClusterIds().add((int) buffer.readUnsignedInt());
      }
    }
    catch (final RuntimeException e)
    {
      throw new OptionalAttributeErrorException();
    }
    return attr;
  }

  private List<PathAttribute> decodePathAttributes(final ByteBuf buffer)
  {
    final List<PathAttribute> attributes = new LinkedList<PathAttribute>();

    while (buffer.isReadable())
    {
      buffer.markReaderIndex();

      try
      {
        final int flagsType = buffer.readUnsignedShort();
        final boolean optional = ((flagsType & BGPv4Constants.BGP_PATH_ATTRIBUTE_OPTIONAL_BIT) != 0);
        final boolean transitive = ((flagsType & BGPv4Constants.BGP_PATH_ATTRIBUTE_TRANSITIVE_BIT) != 0);
        final boolean partial = ((flagsType & BGPv4Constants.BGP_PATH_ATTRIBUTE_PARTIAL_BIT) != 0);
        final int typeCode = (flagsType & BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_MASK);
        int valueLength = 0;

        if ((flagsType & BGPv4Constants.BGP_PATH_ATTRIBUTE_EXTENDED_LENGTH_BIT) != 0)
        {
          valueLength = buffer.readUnsignedShort();
        }
        else
        {
          valueLength = buffer.readUnsignedByte();
        }

        final ByteBuf valueBuffer = Unpooled.buffer(valueLength);

        buffer.readBytes(valueBuffer);

        PathAttribute attr = null;

        switch (typeCode)
        {
          case BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_AGGREGATOR:
            attr = this.decodeAggregatorPathAttribute(valueBuffer, ASType.AS_NUMBER_2OCTETS);
            break;
          case BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_AS4_AGGREGATOR:
            attr = this.decodeAggregatorPathAttribute(valueBuffer, ASType.AS_NUMBER_4OCTETS);
            break;
          case BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_AS4_PATH:
            attr = this.decodeASPathAttribute(valueBuffer, ASType.AS_NUMBER_4OCTETS);
            break;
          case BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_AS_PATH:
            attr = this.decodeASPathAttribute(valueBuffer, ASType.AS_NUMBER_2OCTETS);
            break;
          case BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_ATOMIC_AGGREGATE:
            attr = this.decodeAtomicAggregatePathAttribute(valueBuffer);
            break;
          case BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_COMMUNITIES:
            attr = this.decodeCommunityPathAttribute(valueBuffer);
            break;
          case BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_LOCAL_PREF:
            attr = this.decodeLocalPrefPathAttribute(valueBuffer);
            break;
          case BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_MULTI_EXIT_DISC:
            attr = this.decodeMultiExitDiscPathAttribute(valueBuffer);
            break;
          case BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_NEXT_HOP:
            attr = this.decodeNextHopPathAttribute(valueBuffer);
            break;
          case BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_ORIGIN:
            attr = this.decodeOriginPathAttribute(valueBuffer);
            break;
          case BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_MP_REACH_NLRI:
            attr = this.decodeMpReachNlriPathAttribute(valueBuffer);
            break;
          case BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_MP_UNREACH_NLRI:
            attr = this.decodeMpUnreachNlriPathAttribute(valueBuffer);
            break;
          case BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_ORIGINATOR_ID:
            attr = this.decodeOriginatorIDPathAttribute(valueBuffer);
            break;
          case BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_CLUSTER_LIST:
            attr = this.decodeClusterListPathAttribute(valueBuffer);
            break;
          case BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_EXTENDED_COMMUNITIES:
            attr = this.decodeExtendedCommunityPathAttribute(valueBuffer);
            break;
          default:
          {
            final byte[] value = new byte[valueBuffer.readableBytes()];

            valueBuffer.readBytes(value);
            attr = new UnknownPathAttribute(typeCode, value);
          }
          break;
        }
        attr.setOptional(optional);
        attr.setTransitive(transitive);
        attr.setPartial(partial);

        attributes.add(attr);
      }
      catch (final AttributeException ex)
      {
        final int endReadIndex = buffer.readerIndex();

        buffer.resetReaderIndex();

        final int attributeLength = endReadIndex - buffer.readerIndex();
        final byte[] packet = new byte[attributeLength];

        buffer.readBytes(packet);
        ex.setOffendingAttribute(packet);

        throw ex;
      }
      catch (final IndexOutOfBoundsException ex)
      {
        final int endReadIndex = buffer.readerIndex();

        buffer.resetReaderIndex();

        final int attributeLength = endReadIndex - buffer.readerIndex();
        final byte[] packet = new byte[attributeLength];

        buffer.readBytes(packet);

        throw new AttributeLengthException(packet);
      }

    }

    return attributes;
  }

  private List<NetworkLayerReachabilityInformation> decodeWithdrawnRoutes(final ByteBuf buffer)
  {
    final List<NetworkLayerReachabilityInformation> routes = new LinkedList<NetworkLayerReachabilityInformation>();

    while (buffer.isReadable())
    {
      routes.add(NLRICodec.decodeNLRI(buffer));
    }
    return routes;
  }

}
