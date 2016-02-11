/**
 *  Copyright 2012 Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");˙
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
package io.netlibs.bgp.netty.protocol.update;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.netlibs.bgp.netty.protocol.BGPv4Packet;
import io.netlibs.bgp.netty.protocol.BGPv4PacketVisitor;
import io.netlibs.bgp.protocol.BGPv4Constants;
import io.netlibs.bgp.protocol.NLRICodec;
import io.netlibs.bgp.protocol.NetworkLayerReachabilityInformation;
import io.netlibs.bgp.protocol.attributes.MultiProtocolReachableNLRI;
import io.netlibs.bgp.protocol.attributes.MultiProtocolUnreachableNLRI;
import io.netlibs.bgp.protocol.attributes.PathAttribute;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.ToString;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@ToString
public class UpdatePacket extends BGPv4Packet
{

  private List<NetworkLayerReachabilityInformation> withdrawnRoutes = new LinkedList<NetworkLayerReachabilityInformation>();
  private List<NetworkLayerReachabilityInformation> nlris = new LinkedList<NetworkLayerReachabilityInformation>();
  private List<PathAttribute> pathAttributes = new LinkedList<PathAttribute>();

  @Override
  protected ByteBuf encodePayload()
  {

    final ByteBuf buffer = Unpooled.buffer(BGPv4Constants.BGP_PACKET_MAX_LENGTH);
    final ByteBuf withdrawnBuffer = this.encodeWithdrawnRoutes();
    final ByteBuf pathAttributesBuffer = this.encodePathAttributes();

    buffer.writeShort(withdrawnBuffer.readableBytes());
    buffer.writeBytes(withdrawnBuffer);
    buffer.writeShort(pathAttributesBuffer.readableBytes());
    buffer.writeBytes(pathAttributesBuffer);
    buffer.writeBytes(this.encodeNlris());
    return buffer;
  }

  public int calculatePacketSize()
  {

    int size = BGPv4Constants.BGP_PACKET_MIN_SIZE_UPDATE;

    size += this.calculateSizeWithdrawnRoutes();
    size += this.calculateSizePathAttributes();
    size += this.calculateSizeNlris();

    return size;

  }

  private ByteBuf encodeWithdrawnRoutes()
  {

    final ByteBuf buffer = Unpooled.buffer(BGPv4Constants.BGP_PACKET_MAX_LENGTH);

    if (this.withdrawnRoutes != null)
    {
      for (final NetworkLayerReachabilityInformation route : this.withdrawnRoutes)
      {
        buffer.writeBytes(NLRICodec.encodeNLRI(route));
      }
    }

    return buffer;
  }

  private ByteBuf encodePathAttributes()
  {

    final ByteBuf buffer = Unpooled.buffer(BGPv4Constants.BGP_PACKET_MAX_LENGTH);

    // RJS: Need to encode MP_REACH_NLRI first in the message according to the
    // recommendations in RFC 7606.
    
    if (this.pathAttributes != null)
    {
      for (PathAttribute pathAttr: this.pathAttributes)
      {

        if (pathAttr instanceof MultiProtocolReachableNLRI || pathAttr instanceof MultiProtocolUnreachableNLRI){
          buffer.writeBytes(PathAttributeCodec.encodePathAttribute(pathAttr));
        }
      }

      for (PathAttribute pathAttribute : this.pathAttributes)
      {
        if (!(pathAttribute instanceof MultiProtocolReachableNLRI) && !(pathAttribute instanceof MultiProtocolUnreachableNLRI))
        {
          buffer.writeBytes(PathAttributeCodec.encodePathAttribute(pathAttribute));
        }
      }
    }
    
    return buffer;
  }

  private ByteBuf encodeNlris()
  {

    final ByteBuf buffer = Unpooled.buffer(BGPv4Constants.BGP_PACKET_MAX_LENGTH);

    if (this.nlris != null)
    {
      for (final NetworkLayerReachabilityInformation nlri : this.nlris)
      {
        buffer.writeBytes(NLRICodec.encodeNLRI(nlri));
      }
    }

    return buffer;
  }

  private int calculateSizeWithdrawnRoutes()
  {
    int size = 0;

    if (this.withdrawnRoutes != null)
    {
      for (final NetworkLayerReachabilityInformation route : this.withdrawnRoutes)
      {
        size += NLRICodec.calculateEncodedNLRILength(route);
      }
    }

    return size;
  }

  private int calculateSizeNlris()
  {
    int size = 0;

    if (this.nlris != null)
    {
      for (final NetworkLayerReachabilityInformation nlri : this.nlris)
      {
        size += NLRICodec.calculateEncodedNLRILength(nlri);
      }
    }

    return size;
  }

  private int calculateSizePathAttributes()
  {
    int size = 0;

    if (this.pathAttributes != null)
    {
      for (final PathAttribute attr : this.pathAttributes)
      {
        size += PathAttributeCodec.calculateEncodedPathAttributeLength(attr);
      }
    }

    return size;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.BGPv4Packet#getType()
   */
  @Override
  public int getType()
  {
    return BGPv4Constants.BGP_PACKET_TYPE_UPDATE;
  }

  /**
   * @return the withdrawnRoutes
   */
  public List<NetworkLayerReachabilityInformation> getWithdrawnRoutes()
  {
    return this.withdrawnRoutes;
  }

  /**
   * @param withdrawnRoutes
   *          the withdrawnRoutes to set
   */
  public void setWithdrawnRoutes(final List<NetworkLayerReachabilityInformation> withdrawnRoutes)
  {
    this.withdrawnRoutes = withdrawnRoutes;
  }

  /**
   * @return the nlris
   */
  public List<NetworkLayerReachabilityInformation> getNlris()
  {
    return this.nlris;
  }

  /**
   * @param nlris
   *          the nlris to set
   */
  public void setNlris(final List<NetworkLayerReachabilityInformation> nlris)
  {
    this.nlris = nlris;
  }

  /**
   * @return the pathAttributes
   */
  public List<PathAttribute> getPathAttributes()
  {
    return this.pathAttributes;
  }

  /**
   * @param pathAttributes
   *          the pathAttributes to set
   */
  public void setPathAttributes(final List<PathAttribute> pathAttributes)
  {
    this.pathAttributes = pathAttributes;
  }

  /**
   * look up path attributes of a given type passed in this update packet
   */

  @SuppressWarnings("unchecked")
  public <T extends PathAttribute> Set<T> lookupPathAttributes(final Class<T> paClass)
  {
    
    final Set<T> result = new HashSet<T>();

    for (final PathAttribute pa : this.pathAttributes)
    {
      if (pa.getClass().equals(paClass))
      {
        result.add((T) pa);
      }
    }

    return result;
    
  }

  /**
   * get the path attributes of this packet filtered by given PathAttribute classes
   *
   * @param filteredClasses
   * @return
   */

  public Set<PathAttribute> filterPathAttributes(final Class<? extends PathAttribute>... filteredClasses)
  {

    final Set<PathAttribute> attrs = new HashSet<PathAttribute>();
    final Set<Class<? extends PathAttribute>> filter = new HashSet<Class<? extends PathAttribute>>(Arrays.asList(filteredClasses));

    for (final PathAttribute attr : this.getPathAttributes())
    {
      if (!filter.contains(attr.getClass()))
      {
        attrs.add(attr);
      }
    }

    return attrs;
  }
  
  @Override
  public <T> T apply(BGPv4PacketVisitor<T> visitor)
  {
    return visitor.visit(this);
  }
  


}
