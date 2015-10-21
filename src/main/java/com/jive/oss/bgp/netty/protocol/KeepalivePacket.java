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
 */
package com.jive.oss.bgp.netty.protocol;

import com.jive.oss.bgp.netty.BGPv4Constants;

import io.netty.buffer.ByteBuf;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class KeepalivePacket extends BGPv4Packet
{

  @Override
  protected ByteBuf encodePayload()
  {
    return null;
  }

  @Override
  public int getType()
  {
    return BGPv4Constants.BGP_PACKET_TYPE_KEEPALIVE;
  }

  @Override
  public <T> T apply(final BGPv4PacketVisitor<T> visitor)
  {
    return visitor.visit(this);
  }

}
