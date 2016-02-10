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
 * File: org.bgp4j.netty.protocol.refresh.ORFEntryCodec.java
 */
package io.joss.bgp.netty.protocol.refresh;

import io.joss.bgp.net.AddressPrefixBasedORFEntry;
import io.joss.bgp.net.ORFAction;
import io.joss.bgp.net.ORFEntry;
import io.joss.bgp.netty.NLRICodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class ORFEntryCodec {

  /**
   * get the length of the encoded ORF entry in octets
   *
   * @return
   */
  public static final int calculateEncodingLength(final ORFEntry entry) {
    return 1 + calculateORFPayloadEncodingLength(entry);
  }

  private static int calculateORFPayloadEncodingLength(final ORFEntry entry) {
    if(entry instanceof AddressPrefixBasedORFEntry)
    {
      return calculateAddressPrefixBasedORFPayloadEncodingLength((AddressPrefixBasedORFEntry)entry);
    }
    else
    {
      throw new IllegalArgumentException("cannot handle ORFEntry of type " + entry.getClass().getName());
    }
  }

  /**
   * encode the ORF entry
   *
   * @return
   */
  public static final ByteBuf encodeORFEntry(final ORFEntry entry)
  {
    final ByteBuf buffer = Unpooled.buffer(calculateEncodingLength(entry));
    final ByteBuf payload = encodeORFPayload(entry);

    buffer.writeByte((entry.getAction().toCode() << 6) | (entry.getMatch().toCode() << 5));

    if(payload != null)
    {
      buffer.writeBytes(payload);
    }

    return buffer;
  }

  private static ByteBuf encodeORFPayload(final ORFEntry entry)
  {
    if(entry instanceof AddressPrefixBasedORFEntry)
    {
      return encodeAddressPrefixBasedORFPayload((AddressPrefixBasedORFEntry)entry);
    }
    else
    {
      throw new IllegalArgumentException("cannot handle ORFEntry of type " + entry.getClass().getName());
    }
  }

  private static int calculateAddressPrefixBasedORFPayloadEncodingLength(final AddressPrefixBasedORFEntry entry) {
    int size = 0;

    if(entry.getAction() != ORFAction.REMOVE_ALL)
    {
      size += 6 + NLRICodec.calculateEncodedNLRILength(entry.getPrefix()); // 4 octet sequence + 1 octet min length + 1 octet max length + prefix length
    }

    return size;
  }

  private static ByteBuf encodeAddressPrefixBasedORFPayload(final AddressPrefixBasedORFEntry entry)
  {
    ByteBuf buffer = null;

    if(entry.getAction() != ORFAction.REMOVE_ALL) {
      buffer = Unpooled.buffer(calculateAddressPrefixBasedORFPayloadEncodingLength(entry));

      buffer.writeInt(entry.getSequence());
      buffer.writeByte(entry.getMinLength());
      buffer.writeByte(entry.getMaxLength());
      buffer.writeBytes(NLRICodec.encodeNLRI(entry.getPrefix()));
    }

    return buffer;
  }

}
