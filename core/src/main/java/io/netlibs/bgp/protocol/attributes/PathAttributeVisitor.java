package io.netlibs.bgp.protocol.attributes;

public interface PathAttributeVisitor<R>
{

  /**
   * Default ...
   */
  
  R visitUnknownAttribute(UnknownPathAttribute unknown);

  /**
   * 
   */
  
  R visitAggregatorPathAttribute(AggregatorPathAttribute a);

  R visitASPath(ASPathAttribute a);

  R visitCommunityPathAttribute(CommunityPathAttribute a);

  R visitExtendedCommunityPathAttribute(ExtendedCommunityPathAttribute a);

  R visitAtomicAggregatePathAttribute(AtomicAggregatePathAttribute a);

  R visitClusterListPathAttribute(ClusterListPathAttribute a);

  R visitOriginPathAttribute(OriginPathAttribute a);

  R visitOriginatorIDPathAttribute(OriginatorIDPathAttribute a);

  R visitNextHopPathAttribute(NextHopPathAttribute a);

  R visitMultiProtocolUnreachableNLRI(MultiProtocolUnreachableNLRI a);

  R visitMultiProtocolReachableNLRI(MultiProtocolReachableNLRI a);

  R visitMultiExitDiscPathAttribute(MultiExitDiscPathAttribute a);

  R visitLocalPrefPathAttribute(LocalPrefPathAttribute a);

}
