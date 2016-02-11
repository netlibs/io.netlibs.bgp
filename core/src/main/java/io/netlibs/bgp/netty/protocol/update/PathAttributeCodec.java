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
 * File: org.bgp4j.netty.protocol.update.PathAttributeCodec.java
 */
package io.netlibs.bgp.netty.protocol.update;

import java.util.HashMap;
import java.util.Map;

import io.netlibs.bgp.BGPv4Constants;
import io.netlibs.bgp.net.attributes.ASPathAttribute;
import io.netlibs.bgp.net.attributes.AggregatorPathAttribute;
import io.netlibs.bgp.net.attributes.AtomicAggregatePathAttribute;
import io.netlibs.bgp.net.attributes.ClusterListPathAttribute;
import io.netlibs.bgp.net.attributes.CommunityPathAttribute;
import io.netlibs.bgp.net.attributes.ExtendedCommunityPathAttribute;
import io.netlibs.bgp.net.attributes.LocalPrefPathAttribute;
import io.netlibs.bgp.net.attributes.MultiExitDiscPathAttribute;
import io.netlibs.bgp.net.attributes.MultiProtocolReachableNLRI;
import io.netlibs.bgp.net.attributes.MultiProtocolUnreachableNLRI;
import io.netlibs.bgp.net.attributes.NextHopPathAttribute;
import io.netlibs.bgp.net.attributes.OriginPathAttribute;
import io.netlibs.bgp.net.attributes.OriginatorIDPathAttribute;
import io.netlibs.bgp.net.attributes.PathAttribute;
import io.netlibs.bgp.net.attributes.UnknownPathAttribute;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class PathAttributeCodec
{

  private static Map<Class<? extends PathAttribute>, PathAttributeCodecHandler<? extends PathAttribute>> codecs;

  static
  {
    codecs = new HashMap<Class<? extends PathAttribute>, PathAttributeCodecHandler<? extends PathAttribute>>();

    codecs.put(AggregatorPathAttribute.class, new AggregatorPathAttributeCodecHandler());
    codecs.put(ASPathAttribute.class, new ASPathAttributeCodecHandler());
    codecs.put(AtomicAggregatePathAttribute.class, new AtomicAggregatePathAttributeCodecHandler());
    codecs.put(ClusterListPathAttribute.class, new ClusterListPathAttributeCodecHandler());
    codecs.put(CommunityPathAttribute.class, new CommunityPathAttributeCodecHandler());
    codecs.put(ExtendedCommunityPathAttribute.class, new ExtendedCommunityPathAttributeCodecHandler());
    codecs.put(LocalPrefPathAttribute.class, new LocalPrefPathAttributeCodecHandler());
    codecs.put(MultiExitDiscPathAttribute.class, new MultiExitDiscPathAttributeCodecHandler());
    codecs.put(MultiProtocolReachableNLRI.class, new MultiProtocolReachableNLRICodecHandler());
    codecs.put(MultiProtocolUnreachableNLRI.class, new MultiProtocolUnreachableNLRICodecHandler());
    codecs.put(NextHopPathAttribute.class, new NextHopPathAttributeCodecHandler());
    codecs.put(OriginatorIDPathAttribute.class, new OriginatorIDPathAttributeCodecHandler());
    codecs.put(OriginPathAttribute.class, new OriginPathAttributeCodecHandler());
    codecs.put(UnknownPathAttribute.class, new UnknownPathAttributeCodecHandler());
  }

  /**
   * encode the path attribute for network transmission
   *
   * @return an encoded formatted path attribute
   */
  public static ByteBuf encodePathAttribute(final PathAttribute attr)
  {
    final ByteBuf buffer = Unpooled.buffer(BGPv4Constants.BGP_PACKET_MAX_LENGTH);
    final int valueLength = valueLength(attr);
    int attrFlagsCode = 0;

    if (attr.isOptional())
    {
      attrFlagsCode |= BGPv4Constants.BGP_PATH_ATTRIBUTE_OPTIONAL_BIT;
    }

    if (attr.isTransitive())
    {
      attrFlagsCode |= BGPv4Constants.BGP_PATH_ATTRIBUTE_TRANSITIVE_BIT;
    }

    if (attr.isPartial())
    {
      attrFlagsCode |= BGPv4Constants.BGP_PATH_ATTRIBUTE_PARTIAL_BIT;
    }

    if (valueLength > 255)
    {
      attrFlagsCode |= BGPv4Constants.BGP_PATH_ATTRIBUTE_EXTENDED_LENGTH_BIT;
    }

    attrFlagsCode |= (typeCode(attr) & BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_MASK);

    buffer.writeShort(attrFlagsCode);

    if (valueLength > 255)
    {
      buffer.writeShort(valueLength);
    }
    else
    {
      buffer.writeByte(valueLength);
    }

    if (valueLength > 0)
    {
      buffer.writeBytes(encodeValue(attr));
    }

    return buffer;
  }

  public static int calculateEncodedPathAttributeLength(final PathAttribute attr)
  {
    int size = 2; // attribute flags + type field;
    final int valueLength = valueLength(attr);

    size += (valueLength > 255) ? 2 : 1; // length field;
    size += valueLength;

    return size;
  }

  /**
   * get the attribute value length
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  public static int valueLength(final PathAttribute attr)
  {
    if (codecs.containsKey(attr.getClass()))
    {
      return ((PathAttributeCodecHandler<PathAttribute>) codecs.get(attr.getClass())).valueLength(attr);
    }
    else
    {
      throw new IllegalArgumentException("cannot handle path attribute of type: " + attr.getClass().getName());
    }
  }

  /**
   * get the specific type code (see RFC 4271)
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  public static int typeCode(final PathAttribute attr)
  {
    if (codecs.containsKey(attr.getClass()))
    {
      return ((PathAttributeCodecHandler<PathAttribute>) codecs.get(attr.getClass())).typeCode(attr);
    }
    else
    {
      throw new IllegalArgumentException("cannot handle path attribute of type: " + attr.getClass().getName());
    }
  }

  /**
   * get the encoded attribute value
   */
  @SuppressWarnings("unchecked")
  public static ByteBuf encodeValue(final PathAttribute attr)
  {
    if (codecs.containsKey(attr.getClass()))
    {
      return ((PathAttributeCodecHandler<PathAttribute>) codecs.get(attr.getClass())).encodeValue(attr);
    }
    else
    {
      throw new IllegalArgumentException("cannot handle path attribute of type: " + attr.getClass().getName());
    }
  }

}
