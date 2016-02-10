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
 * File: org.bgp4j.netty.protocol.update.CommunitiesPathAttribute.java
 */
package io.netlibs.bgp.net.attributes;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Refactored by rjs, to remove 'special' first community.
 *
 */

public class CommunityPathAttribute extends PathAttribute
{

  private List<CommunityMember> members = new LinkedList<CommunityMember>();

  public CommunityPathAttribute()
  {
    super(Category.OPTIONAL_TRANSITIVE);
  }


  public CommunityPathAttribute(final List<CommunityMember> members)
  {
    super(Category.OPTIONAL_TRANSITIVE);
    this.members.addAll(members);
  }
  
  /**
   * @return the members
   */
  public List<CommunityMember> getMembers()
  {
    return this.members;
  }

  /**
   * @param members
   *          the members to set
   */
  public void setMembers(final List<CommunityMember> members)
  {
    this.members = members;
  }
  
  public void addMember(CommunityMember member){
    if (members == null){
      this.members = new LinkedList<CommunityMember>();
    }
    this.members.add(member);
  }

  @Override
  protected PathAttributeType internalType()
  {
    return PathAttributeType.COMMUNITY;
  }

  @Override
  protected boolean subclassEquals(final PathAttribute obj)
  {
    final CommunityPathAttribute o = (CommunityPathAttribute) obj;

    final EqualsBuilder builder = (new EqualsBuilder())
        .append(this.getMembers().size(), o.getMembers().size());

    if (builder.isEquals())
    {
      final Iterator<CommunityMember> lit = this.getMembers().iterator();
      final Iterator<CommunityMember> rit = o.getMembers().iterator();

      while (lit.hasNext())
      {
        builder.append(lit.next(), rit.next());
      }
    }

    return builder.isEquals();
  }

  @Override
  protected int subclassHashCode()
  {
    final HashCodeBuilder builder = (new HashCodeBuilder());
    final Iterator<CommunityMember> it = this.getMembers().iterator();

    while (it.hasNext())
    {
      builder.append(it.next());
    }

    return builder.toHashCode();
  }

  @Override
  protected int subclassCompareTo(final PathAttribute obj)
  {
    final CommunityPathAttribute o = (CommunityPathAttribute) obj;
    final CompareToBuilder builder = (new CompareToBuilder())
        .append(this.getMembers().size(), o.getMembers().size());

    if (builder.toComparison() == 0)
    {
      final Iterator<CommunityMember> lit = this.getMembers().iterator();
      final Iterator<CommunityMember> rit = o.getMembers().iterator();

      while (lit.hasNext())
      {
        builder.append(lit.next(), rit.next());
      }
    }

    return builder.toComparison();
  }

  @Override
  protected ToStringBuilder subclassToString()
  {
    final ToStringBuilder builder = new ToStringBuilder(this);

    for (final CommunityMember c : this.members)
    {
      builder.append("member", c);
    }

    return builder;
  }

  @Override
  public <R> R apply(PathAttributeVisitor<R> visitor)
  {
    return visitor.visitCommunityPathAttribute(this);
  }
  
}

