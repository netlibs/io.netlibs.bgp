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
package io.netlibs.bgp.netty.protocol.update;

import io.netlibs.bgp.BGPv4Constants;
import io.netlibs.bgp.netty.protocol.NotificationPacket;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class UpdateNotificationPacket extends NotificationPacket
{

  public static final int SUBCODE_MALFORMED_ATTRIBUTE_LIST = 1;
  public static final int SUBCODE_UNRECOGNIZED_WELL_KNOWN_ATTRIBUTE = 2;
  public static final int SUBCODE_MISSING_WELL_KNOWN_ATTRIBUTE = 3;
  public static final int SUBCODE_ATTRIBUTE_FLAGS_ERROR = 4;
  public static final int SUBCODE_ATTRIBUTE_LENGTH_ERROR = 5;
  public static final int SUBCODE_INVALID_ORIGIN_ATTRIBUTE = 6;
  public static final int SUBCODE_INVALID_NEXT_HOP_ATTRIBUTE = 8;
  public static final int SUBCODE_OPTIONAL_ATTRIBUTE_ERROR = 9;
  public static final int SUBCODE_INVALID_NETWORK_FIELD = 10;
  public static final int SUBCODE_MALFORMED_AS_PATH = 11;

  protected UpdateNotificationPacket(final int subcode)
  {
    super(BGPv4Constants.BGP_ERROR_CODE_UPDATE, subcode);
  }

}
