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
 * File: org.bgp4j.netty.protocol.update.MultiExitDiscPathAttribute.java 
 */
package com.jive.oss.bgp.net.attributes;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import lombok.ToString;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@ToString
public class MultiExitDiscPathAttribute extends PathAttribute
{

  public MultiExitDiscPathAttribute()
  {
    super(Category.OPTIONAL_NON_TRANSITIVE);
  }

  public MultiExitDiscPathAttribute(final int discriminator)
  {
    super(Category.OPTIONAL_NON_TRANSITIVE);

    this.discriminator = discriminator;
  }

  private int discriminator;

  /**
   * @return the discriminator
   */
  public int getDiscriminator()
  {
    return this.discriminator;
  }

  /**
   * @param discriminator
   *          the discriminator to set
   */
  public void setDiscriminator(final int discriminator)
  {
    this.discriminator = discriminator;
  }

  @Override
  protected PathAttributeType internalType()
  {
    return PathAttributeType.MULTI_EXIT_DISC;
  }

  @Override
  protected boolean subclassEquals(final PathAttribute obj)
  {
    final MultiExitDiscPathAttribute o = (MultiExitDiscPathAttribute) obj;

    return (new EqualsBuilder()).append(this.getDiscriminator(), o.getDiscriminator()).isEquals();
  }

  @Override
  protected int subclassHashCode()
  {
    return (new HashCodeBuilder()).append(this.getDiscriminator()).toHashCode();
  }

  @Override
  protected int subclassCompareTo(final PathAttribute obj)
  {
    final MultiExitDiscPathAttribute o = (MultiExitDiscPathAttribute) obj;

    return (new CompareToBuilder()).append(this.getDiscriminator(), o.getDiscriminator()).toComparison();
  }

  @Override
  protected ToStringBuilder subclassToString()
  {
    return (new ToStringBuilder(this))
        .append("discriminator", this.discriminator);
  }
}
