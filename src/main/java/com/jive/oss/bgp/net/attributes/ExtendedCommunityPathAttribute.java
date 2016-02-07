package com.jive.oss.bgp.net.attributes;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ExtendedCommunityPathAttribute extends PathAttribute
{
 
  private List<AbstractExtendedCommunityInterface> members = new LinkedList<AbstractExtendedCommunityInterface>();

  public ExtendedCommunityPathAttribute()
  {
    super(Category.OPTIONAL_TRANSITIVE);
  }
  
  public ExtendedCommunityPathAttribute(AbstractExtendedCommunityInterface member)
  {
    super(Category.OPTIONAL_TRANSITIVE);
    this.members.add(member);
  }
  
  public ExtendedCommunityPathAttribute(List<AbstractExtendedCommunityInterface> members)
  {
    super(Category.OPTIONAL_TRANSITIVE);
    this.members.addAll(members);
  }

  /*
   * @return the list of extended communities
   */
  public List<AbstractExtendedCommunityInterface> getMembers(){
    return this.members;
  }
  
  /*
   * @param member the member to add to the extended community attribute
   */
  public void addMember(AbstractExtendedCommunityInterface member){
    this.members.add(member);
  }
  
  @Override
  protected PathAttributeType internalType()
  {
    return PathAttributeType.EXTENDED_COMMUNITY;
  }
  
  @Override
  protected boolean subclassEquals(PathAttribute obj){
    ExtendedCommunityPathAttribute o = (ExtendedCommunityPathAttribute) obj;
    
    final EqualsBuilder builder = (new EqualsBuilder())
        .append(this.getMembers().size(), o.getMembers().size());
    
    if (builder.isEquals()){
      Iterator<AbstractExtendedCommunityInterface> lit = this.getMembers().iterator();
      Iterator<AbstractExtendedCommunityInterface> rit = o.getMembers().iterator();
      
      while(lit.hasNext())
        builder.append(lit.next(), rit.next());
    }
    
    return builder.isEquals();
  }

  @Override
  protected ToStringBuilder subclassToString()
  {
    final ToStringBuilder builder = new ToStringBuilder(this);
    for (AbstractExtendedCommunityInterface extcomm: this.members){
      builder.append("member: ", extcomm.humanReadable());
    }
    return builder;
  }

  @Override
  protected int subclassHashCode()
  {
    final HashCodeBuilder builder = (new HashCodeBuilder());
    final Iterator<AbstractExtendedCommunityInterface> it = this.getMembers().iterator();
    
    while(it.hasNext())
      builder.append(it.next());
    
    return builder.toHashCode();
  }

  @Override
  protected int subclassCompareTo(PathAttribute obj)
  {
    final ExtendedCommunityPathAttribute o = (ExtendedCommunityPathAttribute) obj;
    final CompareToBuilder builder =  (new CompareToBuilder())
        .append(this.getMembers().size(), o.getMembers().size());
    
    if (builder.toComparison() == 0)
    {
      final Iterator<AbstractExtendedCommunityInterface> lit = this.getMembers().iterator();
      final Iterator<AbstractExtendedCommunityInterface> rit = o.getMembers().iterator();
      
      while(lit.hasNext())
        builder.append(lit.next(), rit.next());
    }
    
    return builder.toComparison();
  }
  
  @Override
  public <R> R apply(PathAttributeVisitor<R> visitor)
  {
    return visitor.visitExtendedCommunityPathAttribute(this);
  }
  
}
