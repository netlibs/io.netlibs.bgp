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
 * File: org.bgp4j.netty.protocol.update.AggregatorPathAttribute.java
 */
package io.netlibs.bgp.net.attributes;

import java.net.Inet4Address;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import io.netlibs.bgp.net.ASType;
import io.netlibs.bgp.net.ASTypeAware;
import io.netlibs.bgp.net.InetAddressComparator;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

public class AggregatorPathAttribute extends PathAttribute implements ASTypeAware
{

  private final ASType asType;
  private int asNumber;
  private Inet4Address aggregator;

  public AggregatorPathAttribute(final ASType asType)
  {
    super(Category.OPTIONAL_TRANSITIVE);

    this.asType = asType;
  }

  public AggregatorPathAttribute(final ASType asType, final int asNumber, final Inet4Address aggregator)
  {
    this(asType);

    this.asNumber = asNumber;
    this.aggregator = aggregator;
  }

  /**
   * @return the fourByteASNumber
   */
  public boolean isFourByteASNumber()
  {
    return (this.asType == ASType.AS_NUMBER_4OCTETS);
  }

  /**
   * @return the asType
   */
  @Override
  public ASType getAsType()
  {
    return this.asType;
  }

  /**
   * @return the asNumber
   */
  public int getAsNumber()
  {
    return this.asNumber;
  }

  /**
   * @param asNumber
   *          the asNumber to set
   */
  public void setAsNumber(final int asNumber)
  {
    this.asNumber = asNumber;
  }

  /**
   * @return the aggregator
   */
  public Inet4Address getAggregator()
  {
    return this.aggregator;
  }

  /**
   * @param aggregator
   *          the aggregator to set
   */
  public void setAggregator(final Inet4Address aggregator)
  {
    this.aggregator = aggregator;
  }

  @Override
  protected PathAttributeType internalType()
  {
    return PathAttributeType.AGGREGATOR;
  }

  @Override
  protected boolean subclassEquals(final PathAttribute obj)
  {
    final AggregatorPathAttribute o = (AggregatorPathAttribute) obj;

    return (new EqualsBuilder())
        .append(this.getAsNumber(), o.getAsNumber())
        .append(this.getAggregator(), o.getAggregator())
        .append(this.getAsType(), o.getAsType())
        .isEquals();
  }

  @Override
  protected int subclassHashCode()
  {
    return (new HashCodeBuilder())
        .append(this.getAsNumber())
        .append(this.getAggregator())
        .append(this.getAsType())
        .toHashCode();
  }

  @Override
  protected int subclassCompareTo(final PathAttribute obj)
  {
    final AggregatorPathAttribute o = (AggregatorPathAttribute) obj;
    return (new CompareToBuilder())
        .append(this.getAsNumber(), o.getAsNumber())
        .append(this.getAggregator(), o.getAggregator(), new InetAddressComparator())
        .append(this.getAsType(), o.getAsType())
        .toComparison();
  }

  @Override
  protected ToStringBuilder subclassToString()
  {
    return (new ToStringBuilder(this))
        .append("asNumber", this.asNumber)
        .append("aggregator", this.aggregator)
        .append("asType", this.asType);
  }

  @Override
  public <R> R apply(PathAttributeVisitor<R> visitor)
  {
    return visitor.visitAggregatorPathAttribute(this);
  }

}
