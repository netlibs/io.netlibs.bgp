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
package org.bgp4j.netty.handlers;

import java.util.List;

import org.bgp4j.netty.BGPv4Constants;
import org.bgp4j.netty.protocol.BadMessageLengthNotificationPacket;
import org.bgp4j.netty.protocol.ConnectionNotSynchronizedNotificationPacket;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Reframing decoder to ensure that a complete BGPv4 packet is processed in the subsequent decoder.
 * 
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@Slf4j
public class BGPv4Reframer extends ByteToMessageDecoder
{

  public static final String HANDLER_NAME = "BGP4-REFRAMER";

  /**
   * reframe the received packet to completely contain the next BGPv4 packet. It peeks into the first four bytes of the TCP stream which
   * contain a 16-bit marker and a 16-bit length field. The marker must be all one's and the length value must be between 19 and 4096
   * according to RFC 4271. The marker and length constraints are verified and if either is violated the connection is closed early.
   * 
   * @param ctx
   *          the context
   * @param channel
   *          the channel from which the data is consumed
   * @param buffer
   *          the buffer to read from
   * @return a complete BGPv4 protocol packet in a channel buffer or null. If a packet is returned it starts on the type byte.
   */

  @Override
  protected void decode(final ChannelHandlerContext ctx, final ByteBuf buffer, final List<Object> out) throws Exception
  {

    if (buffer.readableBytes() < (BGPv4Constants.BGP_PACKET_MIN_LENGTH - 1))
    {
      return;
    }

    buffer.markReaderIndex();

    final byte[] marker = new byte[BGPv4Constants.BGP_PACKET_MARKER_LENGTH];
    
    buffer.readBytes(marker);

    for (final byte element : marker)
    {
      if (element != (byte) 0xff)
      {
        log.error("received invalid marker {}, closing connection", element);
        NotificationHelper.sendEncodedNotification(ctx,
            new ConnectionNotSynchronizedNotificationPacket(),
            new BgpEventFireChannelFutureListener(ctx));

        return;
      }
    }

    // read the packet length.
    final int length = buffer.readUnsignedShort();

    if ((length < BGPv4Constants.BGP_PACKET_MIN_LENGTH) || (length > BGPv4Constants.BGP_PACKET_MAX_LENGTH))
    {

      log.error("received illegal packet size {}, must be between {} and {}. closing connection",
          new Object[] { length, BGPv4Constants.BGP_PACKET_MIN_LENGTH, BGPv4Constants.BGP_PACKET_MAX_LENGTH });

      NotificationHelper.sendEncodedNotification(ctx,
          new BadMessageLengthNotificationPacket(length),
          new BgpEventFireChannelFutureListener(ctx));

      return;

    }

    final int mustRead = (length - (BGPv4Constants.BGP_PACKET_MARKER_LENGTH + 2)); // we have consumed marker and length at this point

    if (buffer.readableBytes() < mustRead)
    {
      buffer.resetReaderIndex();
      return;
    }

    out.add(buffer.readBytes(mustRead));

  }

}
