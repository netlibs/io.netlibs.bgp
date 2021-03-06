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
package io.netlibs.bgp.netty.protocol;

import io.netlibs.bgp.protocol.BGPv4Constants;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class MessageHeaderErrorNotificationPacket extends NotificationPacket
{

  public static final int SUBCODE_CONNECTION_NOT_SYNCHRONIZED = 1;
  public static final int SUBCODE_BAD_MESSAGE_LENGTH = 2;
  public static final int SUBCODE_BAD_MESSAGE_TYPE = 3;

  protected MessageHeaderErrorNotificationPacket(final int subcode)
  {
    super(BGPv4Constants.BGP_ERROR_CODE_MESSAGE_HEADER, subcode);
  }

}
