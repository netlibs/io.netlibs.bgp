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

import java.util.Iterator;
import java.util.List;

import io.joss.bgp.netty.protocol.NotificationPacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

/**
 * This helper class contains static methods for sending notifications.
 *
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class NotificationHelper
{

  private static class ConcatenatedWrite implements ChannelFutureListener
  {
    private final NotificationPacket notification;
    private final ChannelFutureListener next;

    private ConcatenatedWrite(final NotificationPacket notification, final ChannelFutureListener next)
    {
      this.next = next;
      this.notification = notification;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.netty.channel.ChannelFutureListener#operationComplete(org.jboss.netty.channel.ChannelFuture)
     */
    @Override
    public void operationComplete(final ChannelFuture future) throws Exception
    {
      this.send(future.channel());
    }

    private void send(final Channel channel)
    {
      if (this.next != null)
      {
        channel.write(this.notification).addListener(this.next);
      }
      else
      {
        channel.write(this.notification);
      }
    }
  }

  /**
   * send a notification and close the channel after the message was sent.
   *
   * @param ctx
   *          the channel handler context containing the channel.
   * @param notification
   *          the notification to send
   */
  public static void sendNotification(final ChannelHandlerContext ctx, final NotificationPacket notification, final ChannelFutureListener listener)
  {
    sendNotification(ctx.channel(), notification, listener);
  }

  /**
   * send a notification and close the channel after the message was sent.
   *
   * @param channel
   *          the channel.
   * @param notification
   *          the notification to send
   */
  public static void sendNotification(final Channel channel, final NotificationPacket notification, final ChannelFutureListener listener)
  {
    if (listener instanceof BgpEventFireChannelFutureListener)
    {
      ((BgpEventFireChannelFutureListener) listener).setBgpEvent(new NotificationEvent(notification));
    }

    if (listener != null)
    {
      channel.write(notification).addListener(listener);
    }
    else
    {
      channel.write(notification);
    }
  }

  /**
   * send a list of notifications and close the channel after the last message was sent.
   *
   * @param channel
   *          the channel.
   * @param notification
   *          the notification to send
   */
  public static void sendNotifications(final Channel channel, final List<NotificationPacket> notifications, final ChannelFutureListener listener)
  {
    if (listener instanceof BgpEventFireChannelFutureListener)
    {
      ((BgpEventFireChannelFutureListener) listener).setBgpEvent(new NotificationEvent(notifications));
    }

    final Iterator<NotificationPacket> it = notifications.iterator();

    if (it.hasNext())
    {
      ConcatenatedWrite next = new ConcatenatedWrite(it.next(), listener);

      while (it.hasNext())
      {
        final ConcatenatedWrite current = new ConcatenatedWrite(it.next(), next);

        next = current;
      }

      next.send(channel);
    }
  }

  /**
   * send a list of notifications and close the channel after the last message was sent.
   *
   * @param channel
   *          the channel.
   * @param notification
   *          the notification to send
   */
  public static void sendNotifications(final ChannelHandlerContext ctx, final List<NotificationPacket> notifications, final ChannelFutureListener listener)
  {
    sendNotifications(ctx.channel(), notifications, listener);
  }

  /**
   * send a notification and close the channel after the message was sent.
   *
   * @param ctx
   *          the channel handler context containing the channel.
   * @param notification
   *          the notification to send
   */
  public static void sendEncodedNotification(final ChannelHandlerContext ctx, final NotificationPacket notification, final ChannelFutureListener listener)
  {
    sendEncodedNotification(ctx.channel(), notification, listener);
  }

  /**
   * send a notification and close the channel after the message was sent.
   *
   * @param channel
   *          the channel.
   * @param notification
   *          the notification to send
   */
  public static void sendEncodedNotification(final Channel channel, final NotificationPacket notification, final ChannelFutureListener listener)
  {
    if (listener instanceof BgpEventFireChannelFutureListener)
    {
      ((BgpEventFireChannelFutureListener) listener).setBgpEvent(new NotificationEvent(notification));
    }

    if (listener != null)
    {
      channel.write(notification.encodePacket()).addListener(listener);
    }
    else
    {
      channel.write(notification.encodePacket());
    }
  }
}
