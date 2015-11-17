package com.jive.oss.bgp.net.attributes;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
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
    return false;
    
//    EqualsBuilder builder = (new EqualsBuilder())
//        // TODO
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
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  protected int subclassCompareTo(PathAttribute o)
  {
    // TODO Auto-generated method stub
    return 0;
  }
  
  
}
