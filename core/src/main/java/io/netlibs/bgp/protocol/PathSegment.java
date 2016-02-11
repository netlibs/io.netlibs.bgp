package io.netlibs.bgp.protocol;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Joiner;

import lombok.ToString;

@ToString
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

    StringBuilder sb = new StringBuilder();

    switch (this.pathSegmentType)
    {
      case AS_SEQUENCE:
        break;
      case AS_SET:
        sb.append("{ ");
        break;
      case AS_CONFED_SEQUENCE:
        sb.append("( ");
        break;
      case AS_CONFED_SET:
        sb.append("[ ");
        break;
    }

    sb.append(Joiner.on(' ').join(this.ases));

    switch (this.pathSegmentType)
    {
      case AS_SEQUENCE:
        break;
      case AS_SET:
        sb.append(" }");
        break;
      case AS_CONFED_SEQUENCE:
        sb.append(" )");
        break;
      case AS_CONFED_SET:
        sb.append(" ]");
        break;
    }

    return sb.toString();

  }

}