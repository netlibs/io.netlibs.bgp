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
 * File: org.bgp4j.netty.NLRICodec.java
 */
package io.joss.bgp.netty;

import io.joss.bgp.net.NetworkLayerReachabilityInformation;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

public class NLRICodec
{

  /**
   *
   */

  public static NetworkLayerReachabilityInformation decodeNLRI(final ByteBuf buffer)
  {

    final NetworkLayerReachabilityInformation nlri = new NetworkLayerReachabilityInformation();
    final int prefixLength = buffer.readUnsignedByte();
    byte[] prefixBytes = null;

    if (prefixLength > 0)
    {
      prefixBytes = new byte[NetworkLayerReachabilityInformation.calculateOctetsForPrefixLength(prefixLength)];
      buffer.readBytes(prefixBytes);
    }

    nlri.setPrefix(prefixLength, prefixBytes);

    return nlri;

  }

  /**
   *
   */

  public static int calculateEncodedNLRILength(final NetworkLayerReachabilityInformation nlri)
  {
    return NetworkLayerReachabilityInformation.calculateOctetsForPrefixLength(nlri.getPrefixLength()) + 1;
  }

  /**
   * @param nlri
   * @return
   */

  public static ByteBuf encodeNLRI(final NetworkLayerReachabilityInformation nlri)
  {

    final ByteBuf buffer = Unpooled.buffer(calculateEncodedNLRILength(nlri));

    buffer.writeByte(nlri.getPrefixLength());

    if (nlri.getPrefixLength() > 0)
    {
      buffer.writeBytes(nlri.getPrefix());
    }

    return buffer;

  }

}
