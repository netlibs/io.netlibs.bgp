package com.jive.oss.bgp.rib;

import java.util.Arrays;

import com.jive.oss.bgp.net.AddressFamilyKey;
import com.jive.oss.bgp.net.NetworkLayerReachabilityInformation;

public class NlriComparator
{

  public static boolean equals(final Route left, final Route right)
  {
    return equals(left.getAddressFamilyKey(), left.getNlri(), right.getNlri());
  }

  public static boolean isPrefixOf(final Route left, final Route right)
  {
    return isPrefixOf(left.getAddressFamilyKey(), left.getNlri(), right.getNlri());
  }

  public static boolean isPrefixOf(final Route left, final NetworkLayerReachabilityInformation right)
  {
    return isPrefixOf(left.getAddressFamilyKey(), left.getNlri(), right);
  }

  public static boolean equals(final Route left, final NetworkLayerReachabilityInformation right)
  {
    return equals(left.getAddressFamilyKey(), left.getNlri(), right);
  }

  /**
   *
   */

  public static boolean isPrefixOf(final AddressFamilyKey afk, final NetworkLayerReachabilityInformation left, final NetworkLayerReachabilityInformation right)
  {

    if (afk.equals(AddressFamilyKey.IPV4_UNICAST_MPLS_FORWARDING))
    {
      // RFC3107 -  Encoding is:
      //  3-byte label
      //  4-byte IPv4 address
      return isPrefixOf(AddressFamilyKey.IPV4_UNICAST_FORWARDING, left.remove(3), right.remove(3));
    }
    else if (afk.equals(AddressFamilyKey.IPV6_UNICAST_MPLS_FORWARDING))
    {
      // RFC3107 Encoding is:
      //  3-byte label
      //  16-byte IPv6 address
      return isPrefixOf(AddressFamilyKey.IPV6_UNICAST_FORWARDING, left.remove(3), right.remove(3));
    }
    else if (afk.equals(AddressFamilyKey.IPV4_MPLS_VPN_FORWARDING) || afk.equals(AddressFamilyKey.IPV6_MPLS_VPN_FORWARDING))
    {
      // For IPv4 RFC4364 - Encoding is:    label (3-byte) RD (type (2-byte), value(6-byte)) IPv4 (4-byte)
      // For IPv6 RFC4659 - Encoding is:    label (3-byte) RD (type (2-byte), value(6-byte)) IPv6 (16-byte)
         
      // For both AFs - remove label and then extract the subsequent 8-bytes (RD)
      // if the RD is not equal (including the same type), then these
      // are not the same prefix.
      byte[] leftrd = new byte[8];
      byte[] rightrd = new byte[8];
      
      System.arraycopy(left.getPrefix(), 3, leftrd, 0, 8);
      System.arraycopy(right.getPrefix(), 3, rightrd, 0, 8);
      
      if (!Arrays.equals(leftrd, rightrd))
        return false;
  
      if (afk.equals(AddressFamilyKey.IPV4_MPLS_VPN_FORWARDING))
        // Remove both the label and the RD and check whether these are the same IPv4 prefix
        return isPrefixOf(AddressFamilyKey.IPV4_UNICAST_FORWARDING, left.remove(11), right.remove(11));        
      else
        // Remaining case is IPv6 comparison
        return isPrefixOf(AddressFamilyKey.IPV6_UNICAST_FORWARDING, left.remove(11), right.remove(11));
    }
    else
    {
      return left.isPrefixOf(right);
    }

  }

  /**
   *
   */

  public static boolean equals(final AddressFamilyKey afk, final NetworkLayerReachabilityInformation left, final NetworkLayerReachabilityInformation right)
  {
    if (afk.equals(AddressFamilyKey.IPV4_UNICAST_MPLS_FORWARDING))
    {
      return equals(AddressFamilyKey.IPV4_UNICAST_FORWARDING, left.remove(3), right.remove(3));
    }
    else if (afk.equals(AddressFamilyKey.IPV6_UNICAST_MPLS_FORWARDING))
    {
      return equals(AddressFamilyKey.IPV6_UNICAST_FORWARDING, left.remove(3), right.remove(3));
    }
    else if (afk.equals(AddressFamilyKey.IPV4_MPLS_VPN_FORWARDING) || afk.equals(AddressFamilyKey.IPV6_MPLS_VPN_FORWARDING))
    {
      byte[] leftrd = new byte[8];
      byte[] rightrd = new byte[8];
      
      System.arraycopy(left.getPrefix(), 3, leftrd, 0, 8);
      System.arraycopy(right.getPrefix(), 3, rightrd, 0, 8);
      
      if (!Arrays.equals(leftrd, rightrd))
        return false;
      
      // Compare prefix
      if (afk.equals(AddressFamilyKey.IPV4_MPLS_VPN_FORWARDING))
        return equals(AddressFamilyKey.IPV4_UNICAST_FORWARDING, left.remove(11), right.remove(11));
      else
        return equals(AddressFamilyKey.IPV6_UNICAST_FORWARDING, left.remove(11), right.remove(11));
      
    }
    else
    {
      return left.equals(right);
    }
  }

}
