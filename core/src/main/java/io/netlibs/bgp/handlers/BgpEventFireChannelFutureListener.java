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
 * File: org.bgp4j.netty.handlers.BgpEventFireChannelFutureListener.java
 */
package io.netlibs.bgp.handlers;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class BgpEventFireChannelFutureListener implements ChannelFutureListener
{

  private final ChannelHandlerContext upstreamContext;
  private BgpEvent bgpEvent;

  public BgpEventFireChannelFutureListener(final ChannelHandlerContext upstreamContext)
  {
    this.upstreamContext = upstreamContext;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jboss.netty.channel.ChannelFutureListener#operationComplete(org.jboss.netty.channel.ChannelFuture)
   */
  @Override
  public void operationComplete(final ChannelFuture future) throws Exception
  {
    if ((this.upstreamContext != null) && (this.bgpEvent != null))
    {
      this.upstreamContext.fireChannelRead(this.bgpEvent);
    }
  }

  /**
   * @return the bgpEvent
   */
  public BgpEvent getBgpEvent()
  {
    return this.bgpEvent;
  }

  /**
   * @param bgpEvent
   *          the bgpEvent to set
   */
  public void setBgpEvent(final BgpEvent bgpEvent)
  {
    this.bgpEvent = bgpEvent;
  }

}
