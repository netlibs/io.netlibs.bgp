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
package com.jive.oss.bgp.net.attributes;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.jive.oss.bgp.net.Origin;

import lombok.ToString;

/**
 * ORIGIN (type code 1) BGPv4 path attribute
 *
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@ToString
public class OriginPathAttribute extends PathAttribute
{

  private Origin origin;

  public OriginPathAttribute()
  {
    super(Category.WELL_KNOWN_MANDATORY);
    this.origin = Origin.INCOMPLETE;
  }

  public OriginPathAttribute(final Origin origin)
  {
    super(Category.WELL_KNOWN_MANDATORY);
    this.origin = origin;
  }

  /**
   * @return the origin
   */

  public Origin getOrigin()
  {
    return this.origin;
  }

  /**
   * @param origin
   *          the origin to set
   */

  public void setOrigin(final Origin origin)
  {
    this.origin = origin;
  }


  
  @Override
  protected PathAttributeType internalType()
  {
    return PathAttributeType.ORIGIN;
  }

  @Override
  protected boolean subclassEquals(final PathAttribute obj)
  {
    final OriginPathAttribute o = (OriginPathAttribute) obj;

    return (new EqualsBuilder()).append(this.getOrigin(), o.getOrigin()).isEquals();
  }

  @Override
  protected int subclassHashCode()
  {
    return (new HashCodeBuilder()).append(this.getOrigin()).toHashCode();
  }

  @Override
  protected int subclassCompareTo(final PathAttribute obj)
  {
    final OriginPathAttribute o = (OriginPathAttribute) obj;

    return (new CompareToBuilder()).append(this.getOrigin(), o.getOrigin()).toComparison();
  }

  @Override
  protected ToStringBuilder subclassToString()
  {
    return (new ToStringBuilder(this)).append("origin", this.origin);
  }

}
