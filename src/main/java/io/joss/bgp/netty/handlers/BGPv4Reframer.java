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
package io.joss.bgp.netty.handlers;

import java.util.List;

import io.joss.bgp.netty.BGPv4Constants;
import io.joss.bgp.netty.protocol.BadMessageLengthNotificationPacket;
import io.joss.bgp.netty.protocol.ConnectionNotSynchronizedNotificationPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Reads frames from the input, ensuring they are aligned to the boundary.
 */

@Slf4j
public class BGPv4Reframer extends ByteToMessageDecoder
{

  public static final String HANDLER_NAME = "BGPv4-REFRAMER";

  /**
   * reframe the received packet to completely contain the next BGPv4 packet. It peeks into the first four bytes of the TCP stream which
   * contain a 16-bit marker and a 16-bit length field. The marker must be all one's and the length value must be between 19 and 4096
   * according to RFC 4271. The marker and length constraints are verified and if either is violated the connection is closed early.
   * 
   * Any packets that are added start on the type byte. The buffer will contain the full message payload.
   * 
   */

  @Override
  protected void decode(final ChannelHandlerContext ctx, final ByteBuf buffer, final List<Object> out) throws Exception
  {

    if (buffer.readableBytes() < (BGPv4Constants.BGP_PACKET_MIN_LENGTH - 1))
    {
      // need more bytes for a full read.
      return;
    }

    buffer.markReaderIndex();

    // confirm that the next BGP_PACKET_MARKER_LENGTH bytes are all 0xff.

    if (buffer.forEachByte(buffer.readerIndex(), BGPv4Constants.BGP_PACKET_MARKER_LENGTH, value -> value == (byte) 0xff) != -1)
    {
      log.error("received invalid marker, closing connection");
      NotificationHelper.sendEncodedNotification(ctx,
          new ConnectionNotSynchronizedNotificationPacket(),
          new BgpEventFireChannelFutureListener(ctx));
      return;
    }

    // skip the marker.
    buffer.skipBytes(BGPv4Constants.BGP_PACKET_MARKER_LENGTH);

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

    // must if we don't have the right amount, abort.
    if (buffer.readableBytes() < mustRead)
    {
      buffer.resetReaderIndex();
      return;
    }

    out.add(buffer.readBytes(mustRead));

  }

}
