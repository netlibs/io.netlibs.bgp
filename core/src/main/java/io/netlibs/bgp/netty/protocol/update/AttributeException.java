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

import io.netty.buffer.ByteBuf;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public abstract class AttributeException extends UpdatePacketException
{

  /**
   *
   */
  private static final long serialVersionUID = -8265508454292519918L;

  private byte[] offendingAttribute;

  /**
   *
   */
  
  public AttributeException()
  {
  }

  /**
   *
   */
  
  public AttributeException(Exception ex, final byte[] offendingAttribute)
  {
    super(ex);
    this.setOffendingAttribute(offendingAttribute);
  }
  /**
  *
  */
 public AttributeException(final byte[] offendingAttribute)
 {
   this.setOffendingAttribute(offendingAttribute);
 }

  /**
   * @param message
   */
  public AttributeException(final String message, final byte[] offendingAttribute)
  {
    super(message);

    this.setOffendingAttribute(offendingAttribute);
  }

  /**
   *
   */
  public AttributeException(final ByteBuf buffer)
  {
    this.setOffendingAttribute(buffer);
  }

  /**
   * @param message
   */
  public AttributeException(final String message, final ByteBuf buffer)
  {
    super(message);

    this.setOffendingAttribute(buffer);
  }

  /**
   * @return the offendingAttribute
   */
  public byte[] getOffendingAttribute()
  {
    return this.offendingAttribute;
  }

  /**
   * @param offendingAttribute
   *          the offendingAttribute to set
   */
  public void setOffendingAttribute(final byte[] offendingAttribute)
  {
    this.offendingAttribute = offendingAttribute;
  }

  /**
   * @param offendingAttribute
   *          the offendingAttribute to set
   */
  public void setOffendingAttribute(final ByteBuf buffer)
  {
    this.offendingAttribute = new byte[buffer.readableBytes()];

    buffer.readBytes(this.offendingAttribute);
  }
}
