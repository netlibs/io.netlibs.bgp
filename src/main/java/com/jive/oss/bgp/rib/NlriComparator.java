package com.jive.oss.bgp.rib;

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
      // remove the first 3 bytes.
      return isPrefixOf(AddressFamilyKey.IPV4_UNICAST_FORWARDING, left.remove(3), right.remove(3));
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
    else
    {
      return left.equals(right);
    }
  }

}
