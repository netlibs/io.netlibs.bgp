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
 * File: org.bgp4j.netty.protocol.ProtocolPacketUtils.java
 */
package com.jive.oss.bgp.netty.codec;

import com.jive.oss.bgp.netty.BGPv4Constants;
import com.jive.oss.bgp.netty.protocol.ConnectionNotSynchronizedException;

import io.netty.buffer.ByteBuf;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class ProtocolPacketUtils
{

  /**
   * verify the packet size.
   *
   * @param minimumPacketSize
   *          the minimum size in octets the protocol packet must have to be well-formed. If <b>-1</b> is passed the check is not performed.
   * @param maximumPacketSize
   *          the maximum size in octets the protocol packet may have to be well-formed. If <b>-1</b> is passed the check is not performed.
   */
  public static void verifyPacketSize(final ByteBuf buffer, final int minimumPacketSize, final int maximumPacketSize)
  {
    if (minimumPacketSize != -1)
    {
      if (buffer.readableBytes() < (minimumPacketSize - BGPv4Constants.BGP_PACKET_HEADER_LENGTH))
      {
        throw new ConnectionNotSynchronizedException("expected minimum " + (minimumPacketSize - BGPv4Constants.BGP_PACKET_HEADER_LENGTH)
            + " octest, received " + buffer.readableBytes() + " octets");
      }
    }
    if (maximumPacketSize != -1)
    {
      if (buffer.readableBytes() > (maximumPacketSize - BGPv4Constants.BGP_PACKET_HEADER_LENGTH))
      {
        throw new ConnectionNotSynchronizedException("expected maximum " + (maximumPacketSize - BGPv4Constants.BGP_PACKET_HEADER_LENGTH)
            + "octest, received " + buffer.readableBytes() + "octets");
      }
    }
  }

}
