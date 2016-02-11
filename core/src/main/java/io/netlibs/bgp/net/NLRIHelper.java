package io.netlibs.bgp.net;

public class NLRIHelper {
  public static byte[] trimNLRI(int pfxlen, byte[] NLRI){
    // we need to trim the prefix such that it is only the length that is expected
    int expectedOctets = NetworkLayerReachabilityInformation.calculateOctetsForPrefixLength(pfxlen);
    int roundedPrefixLength = (int) Math.ceil(pfxlen/8);
    
    byte[] nprefix = new byte[expectedOctets];
    int offset = NLRI.length - expectedOctets;
    for (int i=0; i<expectedOctets; i++){
      nprefix[i] = NLRI[i];
    }
    return nprefix;
  }
}