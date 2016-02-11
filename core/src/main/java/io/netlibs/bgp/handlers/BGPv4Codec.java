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
package io.netlibs.bgp.handlers;

import java.util.List;

import io.netlibs.bgp.netty.codec.BGPv4PacketDecoder;
import io.netlibs.bgp.netty.codec.BGPv4PacketEncoder;
import io.netlibs.bgp.netty.protocol.BGPv4Packet;
import io.netlibs.bgp.netty.protocol.ProtocolPacketException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

/**
 * Protocol codec which translates between protocol network packets and protocol POJOs
 *
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@Slf4j
public class BGPv4Codec extends ByteToMessageCodec<BGPv4Packet>
{

  public static final String HANDLER_NAME = "BGPv4-Codec";

  private final BGPv4PacketDecoder packetDecoder;

  // set to true when we see an invalid packet.
  private boolean failed = false;

  public BGPv4Codec(BGPv4PacketDecoder instance)
  {
    this.packetDecoder = instance;
  }

  public BGPv4Codec()
  {
    this(BGPv4PacketDecoder.getInstance());
  }

  @Override
  protected void decode(final ChannelHandlerContext ctx, final ByteBuf buffer, final List<Object> out) throws Exception
  {

    
    if (failed)
    {
      return;
    }

    try
    {

      if (!buffer.isReadable())
      {
        return;
      }

      final BGPv4Packet packet = this.packetDecoder.decodePacket(buffer);

      log.trace("Received: {}", packet);

      if (packet != null)
      {
        out.add(packet);
      }

    }
    catch (final ProtocolPacketException ex)
    {

      log.error("received malformed protocol packet, closing connection", ex);
      NotificationHelper.sendNotification(ctx, ex.toNotificationPacket(), new BgpEventFireChannelFutureListener(ctx));
      this.failed = true;

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
    try
    {
      final ByteBuf buffer = msg.apply(new BGPv4PacketEncoder());
      log.debug("Sending: {}", msg);
      ctx.writeAndFlush(buffer);
    }
    catch (Exception ex)
    {
      log.warn("Error encoding packet: {}", msg, ex);
    }
  }

}
