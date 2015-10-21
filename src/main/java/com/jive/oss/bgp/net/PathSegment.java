package com.jive.oss.bgp.net;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class PathSegment implements Comparable<PathSegment>
{

  private ASType asType;
  private List<Integer> ases = new LinkedList<Integer>();
  private PathSegmentType pathSegmentType;

  public PathSegment()
  {
  }

  public PathSegment(final ASType asType)
  {
    this.asType = asType;
  }

  public PathSegment(final ASType asType, final PathSegmentType pathSegmentType, final int[] asArray)
  {
    this(asType);

    this.pathSegmentType = pathSegmentType;

    if (asArray != null)
    {
      for (final int as : asArray)
      {
        this.ases.add(as);
      }
    }
  }

  /**
   * @return the asType
   */
  public ASType getAsType()
  {
    return this.asType;
  }

  /**
   * @return the ases
   */
  public List<Integer> getAses()
  {
    return this.ases;
  }

  /**
   * @param ases
   *          the ases to set
   */
  public void setAses(final List<Integer> ases)
  {
    if (ases != null)
    {
      this.ases = ases;
    }
    else
    {
      this.ases = new LinkedList<Integer>();
    }
  }

  /**
   * @return the type
   */
  public PathSegmentType getPathSegmentType()
  {
    return this.pathSegmentType;
  }

  /**
   * @param type
   *          the type to set
   */
  public void setPathSegmentType(final PathSegmentType type)
  {
    this.pathSegmentType = type;
  }

  @Override
  public int compareTo(final PathSegment o)
  {
    final CompareToBuilder builder = (new CompareToBuilder())
        .append(this.getAsType(), o.getAsType())
        .append(this.getPathSegmentType(), o.getPathSegmentType())
        .append(this.getAses().size(), o.getAses().size());

    if (builder.toComparison() == 0)
    {
      final Iterator<Integer> lit = this.getAses().iterator();
      final Iterator<Integer> rit = o.getAses().iterator();

      while (lit.hasNext())
      {
        builder.append(lit.next(), rit.next());
      }
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
    final HashCodeBuilder builder = (new HashCodeBuilder())
        .append(this.getAsType())
        .append(this.getPathSegmentType());
    final Iterator<Integer> it = this.getAses().iterator();

    while (it.hasNext())
    {
      builder.append(it.next());
    }

    return builder.toHashCode();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj)
  {
    if (!(obj instanceof PathSegment))
    {
      return false;
    }

    final PathSegment o = (PathSegment) obj;

    final EqualsBuilder builder = (new EqualsBuilder())
        .append(this.getAsType(), o.getAsType())
        .append(this.getPathSegmentType(), o.getPathSegmentType())
        .append(this.getAses().size(), o.getAses().size());

    if (builder.isEquals())
    {
      final Iterator<Integer> lit = this.getAses().iterator();
      final Iterator<Integer> rit = o.getAses().iterator();

      while (lit.hasNext())
      {
        builder.append(lit.next(), rit.next());
      }
    }

    return builder.isEquals();
  }

  @Override
  public String toString()
  {
    final ToStringBuilder builder = new ToStringBuilder(this)
        .append("asType", this.asType)
        .append("pathSegmentType", this.pathSegmentType);

    for (final int as : this.ases)
    {
      builder.append("as", as);
    }

    return builder.toString();
  }
}