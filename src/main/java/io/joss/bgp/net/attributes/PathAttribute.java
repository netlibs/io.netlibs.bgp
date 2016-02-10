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
package io.joss.bgp.net.attributes;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Superclass for all BGPv4 path attributes
 *
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

public abstract class PathAttribute implements Comparable<PathAttribute>
{

  /**
   * @author Rainer Bieniek (Rainer.Bieniek@web.de)
   *
   */
  public enum Category
  {
    WELL_KNOWN_MANDATORY, WELL_KNOWN_DISCRETIONARY, OPTIONAL_TRANSITIVE, OPTIONAL_NON_TRANSITIVE,
  }

  private boolean optional;
  private boolean transitive;
  private boolean partial;
  private final Category category;

  protected PathAttribute(final Category category)
  {
    this.category = category;

    switch (category)
    {
      case OPTIONAL_NON_TRANSITIVE:
        this.setTransitive(false);
        this.setOptional(true);
        break;
      case OPTIONAL_TRANSITIVE:
        this.setTransitive(true);
        this.setOptional(true);
        break;
      case WELL_KNOWN_DISCRETIONARY:
        this.setTransitive(true);
        this.setOptional(false);
        break;
      case WELL_KNOWN_MANDATORY:
        this.setTransitive(true);
        this.setOptional(false);
        break;
    }
  }

  /**
   * @return the partial
   */
  public boolean isPartial()
  {
    return this.partial;
  }

  /**
   * @param partial
   *          the partial to set
   */
  public void setPartial(final boolean partial)
  {
    this.partial = partial;
  }

  /**
   * @return the optional
   */
  public boolean isOptional()
  {
    return this.optional;
  }

  /**
   * @return the optional
   */
  public boolean isWellKnown()
  {
    return !this.isOptional();
  }

  /**
   * @param optional
   *          the optional to set
   */
  public void setOptional(final boolean optional)
  {
    this.optional = optional;
  }

  /**
   * @param wellKnown
   *          the well known to set
   */
  protected void setWellKnown(final boolean wellKnown)
  {
    this.setOptional(!wellKnown);
  }

  /**
   * @return the transitive
   */
  public boolean isTransitive()
  {
    return this.transitive;
  }

  /**
   * @param transitive
   *          the transitive to set
   */
  public void setTransitive(final boolean transitive)
  {
    this.transitive = transitive;
  }

  /**
   * @return the category
   */
  public Category getCategory()
  {
    return this.category;
  }

  @Override
  public String toString()
  {
    return this.subclassToString().append("internalType", this.internalType())
        .append("category", this.getCategory())
        .append("option", this.isOptional())
        .append("partial", this.isPartial())
        .append("transitive", this.isTransitive())
        .toString();
  }

  protected abstract ToStringBuilder subclassToString();

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(final PathAttribute o)
  {
    final CompareToBuilder builder = new CompareToBuilder();

    builder.append(this.internalType(), o.internalType())
        .append(this.getCategory(), o.getCategory())
        .append(this.isOptional(), o.isOptional())
        .append(this.isPartial(), o.isPartial())
        .append(this.isTransitive(), o.isTransitive());

    if (this.internalType() == o.internalType())
    {
      builder.appendSuper(this.subclassCompareTo(o));
    }

    return builder.toComparison();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    return (new HashCodeBuilder())
        .append(this.internalType())
        .append(this.getCategory())
        .append(this.isOptional())
        .append(this.isPartial())
        .append(this.isTransitive())
        .appendSuper(this.subclassHashCode())
        .toHashCode();
  }

  public PathAttributeType getType()
  {
    return this.internalType();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj)
  {
    if (!(obj instanceof PathAttribute))
    {
      return false;
    }

    final PathAttribute o = (PathAttribute) obj;

    final EqualsBuilder builder = new EqualsBuilder();

    builder.append(this.internalType(), o.internalType())
        .append(this.getCategory(), o.getCategory())
        .append(this.isOptional(), o.isOptional())
        .append(this.isPartial(), o.isPartial())
        .append(this.isTransitive(), o.isTransitive());

    if (this.internalType() == o.internalType())
    {
      builder.appendSuper(this.subclassEquals(o));
    }

    return builder.isEquals();
  }

  /**
   * get the internal type of the path attribute
   *
   * @return
   */
  protected abstract PathAttributeType internalType();

  /**
   * handles equals on subclass.
   *
   * @param obj
   * @return
   */
  protected abstract boolean subclassEquals(PathAttribute obj);

  /**
   * handle hashCode on subclass
   *
   * @return
   */
  protected abstract int subclassHashCode();

  /**
   * handle compareTo on subclass
   *
   * @param o
   * @return
   */
  protected abstract int subclassCompareTo(PathAttribute o);

  /**
   * 
   */
  

  public abstract <R> R apply(PathAttributeVisitor<R> visitor);

}
