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
package com.jive.oss.bgp.netty.handlers;

import java.util.List;

import com.jive.oss.bgp.netty.codec.BGPv4PacketDecoder;
import com.jive.oss.bgp.netty.codec.BGPv4PacketEncoder;
import com.jive.oss.bgp.netty.protocol.BGPv4Packet;
import com.jive.oss.bgp.netty.protocol.ProtocolPacketException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Protocol codec which translates between protocol network packets and protocol POJOs
 *
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@Slf4j
@AllArgsConstructor
public class BGPv4Codec extends ByteToMessageCodec<BGPv4Packet>
{

  public static final String HANDLER_NAME = "BGPv4-Codec";

  private final BGPv4PacketDecoder packetDecoder;

  @Override
  protected void decode(final ChannelHandlerContext ctx, final ByteBuf buffer, final List<Object> out) throws Exception
  {

    try
    {

      if (!buffer.isReadable())
      {
        return;
      }

      final BGPv4Packet packet = this.packetDecoder.decodePacket(buffer);

      log.debug("Received: {}", packet);

      if (packet != null)
      {
        out.add(packet);
      }

    }
    catch (final ProtocolPacketException ex)
    {
      log.error("received malformed protocol packet, closing connection", ex);
      NotificationHelper.sendNotification(ctx, ex.toNotificationPacket(), new BgpEventFireChannelFutureListener(ctx));
    }
    catch (final Exception ex)
    {
      log.error("generic decoding exception, closing connection", ex);
      ctx.channel().close();
    }

  }

  /**
   * Upstream requested transmission of the given BGPv4 packet. Encode and transmit.
   */

  @Override
  protected void encode(final ChannelHandlerContext ctx, final BGPv4Packet msg, final ByteBuf out) throws Exception
  {
    final ByteBuf buffer = msg.apply(new BGPv4PacketEncoder());
    log.debug("Sending: {}", msg);
    ctx.writeAndFlush(buffer);
  }

}
