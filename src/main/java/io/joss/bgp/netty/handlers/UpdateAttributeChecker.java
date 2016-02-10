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
 * File: org.bgp4j.netty.protocol.UpdateAttributeChecker.java 
 */
package io.joss.bgp.netty.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.joss.bgp.net.ASTypeAware;
import io.joss.bgp.net.attributes.ASPathAttribute;
import io.joss.bgp.net.attributes.LocalPrefPathAttribute;
import io.joss.bgp.net.attributes.NextHopPathAttribute;
import io.joss.bgp.net.attributes.OriginPathAttribute;
import io.joss.bgp.net.attributes.PathAttribute;
import io.joss.bgp.netty.BGPv4Constants;
import io.joss.bgp.netty.PeerConnectionInformation;
import io.joss.bgp.netty.PeerConnectionInformationAware;
import io.joss.bgp.netty.protocol.NotificationPacket;
import io.joss.bgp.netty.protocol.update.AttributeFlagsNotificationPacket;
import io.joss.bgp.netty.protocol.update.MalformedAttributeListNotificationPacket;
import io.joss.bgp.netty.protocol.update.MissingWellKnownAttributeNotificationPacket;
import io.joss.bgp.netty.protocol.update.PathAttributeCodec;
import io.joss.bgp.netty.protocol.update.UpdatePacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Rainer Bieniek (rainer@bgp4j.org)
 *
 */

@Slf4j
@PeerConnectionInformationAware
public class UpdateAttributeChecker extends SimpleChannelInboundHandler<Object>
{

  private Set<Class<? extends PathAttribute>> mandatoryIBGPAttributes = new HashSet<Class<? extends PathAttribute>>();
  private Set<Class<? extends PathAttribute>> mandatoryEBGPAttributes = new HashSet<Class<? extends PathAttribute>>();
  private Map<Class<? extends PathAttribute>, Integer> as2ClazzCodeMap = new HashMap<Class<? extends PathAttribute>, Integer>();
  private Map<Class<? extends PathAttribute>, Integer> as4ClazzCodeMap = new HashMap<Class<? extends PathAttribute>, Integer>();

  private UpdateAttributeChecker()
  {

    mandatoryEBGPAttributes.add(OriginPathAttribute.class);
    mandatoryEBGPAttributes.add(ASPathAttribute.class);
    mandatoryEBGPAttributes.add(NextHopPathAttribute.class);

    mandatoryIBGPAttributes.add(OriginPathAttribute.class);
    mandatoryIBGPAttributes.add(ASPathAttribute.class);
    mandatoryIBGPAttributes.add(NextHopPathAttribute.class);
    mandatoryIBGPAttributes.add(LocalPrefPathAttribute.class);

    as2ClazzCodeMap.put(ASPathAttribute.class, BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_AS_PATH);
    as2ClazzCodeMap.put(LocalPrefPathAttribute.class, BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_LOCAL_PREF);
    as2ClazzCodeMap.put(NextHopPathAttribute.class, BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_NEXT_HOP);
    as2ClazzCodeMap.put(OriginPathAttribute.class, BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_ORIGIN);

    as4ClazzCodeMap.put(ASPathAttribute.class, BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_AS4_PATH);
    as4ClazzCodeMap.put(LocalPrefPathAttribute.class, BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_LOCAL_PREF);
    as4ClazzCodeMap.put(NextHopPathAttribute.class, BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_NEXT_HOP);
    as4ClazzCodeMap.put(OriginPathAttribute.class, BGPv4Constants.BGP_PATH_ATTRIBUTE_TYPE_ORIGIN);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Object e) throws Exception
  {
    boolean sentUpstream = false;

    if (e instanceof UpdatePacket)
    {
      PeerConnectionInformation connInfo = ctx.attr(BGPv4ClientEndpoint.PEER_CONNECTION_INFO).get();
      UpdatePacket update = (UpdatePacket) e;
      List<PathAttribute> attributeFlagsErrorList = new LinkedList<PathAttribute>();
      List<Class<? extends PathAttribute>> missingWellKnownList = new LinkedList<Class<? extends PathAttribute>>();
      Set<Class<? extends PathAttribute>> givenAttributes = new HashSet<Class<? extends PathAttribute>>();

      // check if passed optional / transitive bits match the presettings of the attribute type
      for (PathAttribute attribute : update.getPathAttributes())
      {
        boolean badAttr = false;

        givenAttributes.add(attribute.getClass());

        switch (attribute.getCategory())
        {
          case WELL_KNOWN_MANDATORY:
          case WELL_KNOWN_DISCRETIONARY:
            badAttr = attribute.isOptional() || !attribute.isTransitive();
            break;
          case OPTIONAL_NON_TRANSITIVE:
            badAttr = !attribute.isOptional() || attribute.isTransitive();
            break;
          case OPTIONAL_TRANSITIVE:
            badAttr = !attribute.isOptional() || !attribute.isTransitive();
            break;
        }

        if (badAttr)
        {
          log.info("detected attribute " + attribute + " with invalid flags");

          attributeFlagsErrorList.add(attribute);
        }
      }

      // if we have any bad attribute, generate notification message and leave
      if (attributeFlagsErrorList.size() > 0)
      {
        NotificationHelper.sendNotification(ctx,
            new AttributeFlagsNotificationPacket(serializeAttributes(attributeFlagsErrorList)),
            new BgpEventFireChannelFutureListener(ctx));
      }
      else
      {
        // check presence of mandatory attributes
        Set<Class<? extends PathAttribute>> mandatoryAttributes;

        if (connInfo.isIBGPConnection())
          mandatoryAttributes = mandatoryIBGPAttributes;
        else
          mandatoryAttributes = mandatoryEBGPAttributes;

        for (Class<? extends PathAttribute> attrClass : mandatoryAttributes)
        {
          if (!givenAttributes.contains(attrClass))
          {
            missingWellKnownList.add(attrClass);
          }
        }

        if (missingWellKnownList.size() > 0)
        {
          Map<Class<? extends PathAttribute>, Integer> codeMap;
          List<NotificationPacket> notifications = new LinkedList<NotificationPacket>();

          if (connInfo.isAS4OctetsInUse())
            codeMap = as4ClazzCodeMap;
          else
            codeMap = as2ClazzCodeMap;

          if (connInfo.isAS4OctetsInUse())
            codeMap = as4ClazzCodeMap;
          else
            codeMap = as2ClazzCodeMap;

          for (Class<? extends PathAttribute> attrClass : missingWellKnownList)
          {
            int code = codeMap.get(attrClass);

            log.info("detected missing well-known atribute, type " + code);
            notifications.add(new MissingWellKnownAttributeNotificationPacket(code));
          }

          NotificationHelper.sendNotifications(ctx,
              notifications,
              new BgpEventFireChannelFutureListener(ctx));
        }
        else
        {
          boolean haveBougsWidth = false;

          // check path attributes for AS number width (2 or 4) settings which mismatch the connection configuration
          for (PathAttribute attribute : update.getPathAttributes())
          {
            if (attribute instanceof ASTypeAware)
            {
              if (((ASTypeAware) attribute).getAsType() != connInfo.getAsTypeInUse())
              {
                haveBougsWidth = true;
              }
            }
          }

          if (haveBougsWidth)
          {
            NotificationHelper.sendNotification(ctx,
                new MalformedAttributeListNotificationPacket(),
                new BgpEventFireChannelFutureListener(ctx));
          }
          else
            sentUpstream = true;
        }
      }
    }
    else
      sentUpstream = true;

    if (sentUpstream)
      ctx.fireChannelRead(e);
  }

  private byte[] serializeAttributes(List<PathAttribute> attrs)
  {
    int size = 0;

    for (PathAttribute attr : attrs)
      size += PathAttributeCodec.calculateEncodedPathAttributeLength(attr);

    ByteBuf buffer = Unpooled.buffer(size);

    for (PathAttribute attr : attrs)
      buffer.writeBytes(PathAttributeCodec.encodePathAttribute(attr));

    byte[] b = new byte[size];

    buffer.readBytes(b);

    return b;
  }

}
