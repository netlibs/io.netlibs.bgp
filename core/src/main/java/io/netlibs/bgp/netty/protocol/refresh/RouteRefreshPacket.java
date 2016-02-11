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
 * File: org.bgp4j.netty.protocol.RouteRefreshPacket.java
 */
package io.netlibs.bgp.netty.protocol.refresh;

import org.apache.commons.lang3.builder.ToStringBuilder;

import io.netlibs.bgp.BGPv4Constants;
import io.netlibs.bgp.net.AddressFamily;
import io.netlibs.bgp.net.ORFEntry;
import io.netlibs.bgp.net.ORFType;
import io.netlibs.bgp.net.OutboundRouteFilter;
import io.netlibs.bgp.net.SubsequentAddressFamily;
import io.netlibs.bgp.netty.protocol.BGPv4Packet;
import io.netlibs.bgp.netty.protocol.BGPv4PacketVisitor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class RouteRefreshPacket extends BGPv4Packet {

  private AddressFamily addressFamily;
  private SubsequentAddressFamily subsequentAddressFamily;
  private OutboundRouteFilter outboundRouteFilter;

  public RouteRefreshPacket() {}

  public RouteRefreshPacket(final AddressFamily addressFamily, final SubsequentAddressFamily subsequentAddressFamily) {
    this.setAddressFamily(addressFamily);
    this.setSubsequentAddressFamily(subsequentAddressFamily);
  }

  public RouteRefreshPacket(final AddressFamily addressFamily, final SubsequentAddressFamily subsequentAddressFamily, final OutboundRouteFilter outboundRouteFilter) {
    this(addressFamily, subsequentAddressFamily);

    this.setOutboundRouteFilter(outboundRouteFilter);
  }

  /* (non-Javadoc)
   * @see org.bgp4j.netty.protocol.BGPv4Packet#encodePayload()
   */
  @Override
  protected ByteBuf encodePayload()
  {

    final ByteBuf buffer = Unpooled.buffer(this.calculateEncodingLength());

    buffer.writeShort(this.getAddressFamily().toCode());
    buffer.writeByte(0);
    buffer.writeByte(this.getSubsequentAddressFamily().toCode());

    if(this.outboundRouteFilter != null) {
      buffer.writeByte(this.outboundRouteFilter.getRefreshType().toCode());

      for(final ORFType type : this.outboundRouteFilter.getEntries().keySet()) {
        int entriesLength=0;

        for(final ORFEntry entry : this.outboundRouteFilter.getEntries().get(type))
        {
          entriesLength += ORFEntryCodec.calculateEncodingLength(entry);
        }

        buffer.writeByte(type.toCode());
        buffer.writeShort(entriesLength);

        for(final ORFEntry entry : this.outboundRouteFilter.getEntries().get(type)) {
          buffer.writeBytes(ORFEntryCodec.encodeORFEntry(entry));
        }

      }
    }

    return buffer;
  }

  public int calculateEncodingLength() {
    int size = 4; // 2 octet AFI + 1 octet reserved + 1 octet SAFI

    if(this.outboundRouteFilter != null) {
      size++; // when-to-refresh-octet

      for(final ORFType type : this.outboundRouteFilter.getEntries().keySet()) {
        size += 3;  // 1 octet ORF type + 2 octets ORF entries length

        for(final ORFEntry entry : this.outboundRouteFilter.getEntries().get(type))
        {
          size += ORFEntryCodec.calculateEncodingLength(entry);
        }
      }
    }

    return size;
  }

  /* (non-Javadoc)
   * @see org.bgp4j.netty.protocol.BGPv4Packet#getType()
   */
  @Override
  public int getType() {
    return BGPv4Constants.BGP_PACKET_TYPE_ROUTE_REFRESH;
  }

  /**
   * @return the addressFamily
   */
  public AddressFamily getAddressFamily() {
    return this.addressFamily;
  }

  /**
   * @param addressFamily the addressFamily to set
   */
  public void setAddressFamily(final AddressFamily addressFamily) {
    this.addressFamily = addressFamily;
  }

  /**
   * @return the subsequentAddressFamily
   */
  public SubsequentAddressFamily getSubsequentAddressFamily() {
    return this.subsequentAddressFamily;
  }

  /**
   * @param subsequentAddressFamily the subsequentAddressFamily to set
   */
  public void setSubsequentAddressFamily(
      final SubsequentAddressFamily subsequentAddressFamily) {
    this.subsequentAddressFamily = subsequentAddressFamily;
  }

  /**
   * @return the outboundRouteFilter
   */
  public OutboundRouteFilter getOutboundRouteFilter() {
    return this.outboundRouteFilter;
  }

  /**
   * @param outboundRouteFilter the outboundRouteFilter to set
   */
  public void setOutboundRouteFilter(final OutboundRouteFilter outboundRouteFilter) {
    this.outboundRouteFilter = outboundRouteFilter;
  }

  @Override
  public <T> T apply(final BGPv4PacketVisitor<T> visitor)
  {
    return visitor.visit(this);
  }

  @Override
  public String toString() {
    return (new ToStringBuilder(this))
        .append("type", this.getType())
        .append("addressFamiliy", this.addressFamily)
        .append("outboundRouteFilter", this.outboundRouteFilter)
        .append("subsequentAddressFamily", this.subsequentAddressFamily)
        .toString();
  }
}
