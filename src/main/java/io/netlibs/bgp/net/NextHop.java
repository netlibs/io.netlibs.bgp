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
 * File: org.bgp4j.rib.NextHop.java
 */
package io.netlibs.bgp.net;

/**
 * Generic next-hop information
 *
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public interface NextHop extends Comparable<NextHop>
{

  public enum Type
  {
    InetAddress, Binary;
  }

  /**
   * Equals other instance
   *
   * @param o
   * @return
   */
  @Override
  public boolean equals(Object o);

  /**
   * Hash code of this instance
   *
   * @return
   */
  @Override
  public int hashCode();

  /**
   * get the type of the next hop
   *
   * @return
   */
  public Type getType();
}
