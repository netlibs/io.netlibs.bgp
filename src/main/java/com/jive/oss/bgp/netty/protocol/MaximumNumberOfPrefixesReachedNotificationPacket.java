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
 * File: org.bgp4j.netty.protocol.MaximumNumberOfPrefixesReachedNotificationPacket.java
 */
package com.jive.oss.bgp.netty.protocol;

import com.jive.oss.bgp.net.AddressFamily;
import com.jive.oss.bgp.net.SubsequentAddressFamily;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class MaximumNumberOfPrefixesReachedNotificationPacket extends CeaseNotificationPacket
{

  private AddressFamily addressFamily;
  private SubsequentAddressFamily subsequentAddressFamily;
  private int prefixUpperBound;

  /**
   * @param subcode
   */
  public MaximumNumberOfPrefixesReachedNotificationPacket()
  {
    super(CeaseNotificationPacket.SUBCODE_MAXIMUM_NUMBER_OF_PREFIXES_REACHED);
  }

  /**
   * @param subcode
   */
  public MaximumNumberOfPrefixesReachedNotificationPacket(final AddressFamily addressFamily, final SubsequentAddressFamily subsequentAddressFamily, final int prefixUpperBound)
  {
    super(CeaseNotificationPacket.SUBCODE_MAXIMUM_NUMBER_OF_PREFIXES_REACHED);

    this.addressFamily = addressFamily;
    this.subsequentAddressFamily = subsequentAddressFamily;
    this.prefixUpperBound = prefixUpperBound;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.bgp4j.netty.protocol.NotificationPacket#encodeAdditionalPayload()
   */
  @Override
  protected ByteBuf encodeAdditionalPayload()
  {
    ByteBuf buffer = null;

    if (this.addressFamily != null)
    {
      buffer = Unpooled.buffer(7);

      buffer.writeShort(this.addressFamily.toCode());
      buffer.writeByte(this.subsequentAddressFamily.toCode());
      buffer.writeInt(this.prefixUpperBound);
    }

    return buffer;
  }

  /**
   * @return the addressFamily
   */
  public AddressFamily getAddressFamily()
  {
    return this.addressFamily;
  }

  /**
   * @param addressFamily
   *          the addressFamily to set
   */
  public void setAddressFamily(final AddressFamily addressFamily)
  {
    this.addressFamily = addressFamily;
  }

  /**
   * @return the subsequentAddressFamily
   */
  public SubsequentAddressFamily getSubsequentAddressFamily()
  {
    return this.subsequentAddressFamily;
  }

  /**
   * @param subsequentAddressFamily
   *          the subsequentAddressFamily to set
   */
  public void setSubsequentAddressFamily(
      final SubsequentAddressFamily subsequentAddressFamily)
  {
    this.subsequentAddressFamily = subsequentAddressFamily;
  }

  /**
   * @return the prefixUpperBound
   */
  public int getPrefixUpperBound()
  {
    return this.prefixUpperBound;
  }

  /**
   * @param prefixUpperBound
   *          the prefixUpperBound to set
   */
  public void setPrefixUpperBound(final int prefixUpperBound)
  {
    this.prefixUpperBound = prefixUpperBound;
  }

}
