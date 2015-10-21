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
 * File: org.bgp4j.netty.protocol.update.PathAttributeCodecHandler.java
 */
package org.bgp4j.netty.protocol.update;

import org.bgp4j.net.attributes.PathAttribute;

import io.netty.buffer.ByteBuf;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public abstract class PathAttributeCodecHandler<T extends PathAttribute> {

  public abstract int typeCode(T attr);

  public abstract int valueLength(T attr);

  public abstract ByteBuf encodeValue(T attr);
}
