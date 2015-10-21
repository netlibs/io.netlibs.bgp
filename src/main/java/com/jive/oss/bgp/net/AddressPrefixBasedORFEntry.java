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
 * File: org.bgp4j.netty.protocol.refresh.AddressPrefixORFEntry.java
 */
package com.jive.oss.bgp.net;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

public class AddressPrefixBasedORFEntry extends ORFEntry
{

  @Getter
  @Setter
  private int sequence;

  @Getter
  @Setter
  private int minLength;

  @Getter
  @Setter
  private int maxLength;

  @Getter
  @Setter
  private NetworkLayerReachabilityInformation prefix;

  public AddressPrefixBasedORFEntry(final ORFAction action, final ORFMatch match)
  {
    super(action, match);
  }

  public AddressPrefixBasedORFEntry(
      final ORFAction action,
      final ORFMatch match,
      final int sequence,
      final int minLength,
      final int maxLength,
      final NetworkLayerReachabilityInformation prefix)
  {
    super(action, match);
    this.sequence = sequence;
    this.minLength = minLength;
    this.maxLength = maxLength;
    this.prefix = prefix;
  }

  @Override
  public ORFType getORFType()
  {
    return ORFType.ADDRESS_PREFIX_BASED;
  }

}
