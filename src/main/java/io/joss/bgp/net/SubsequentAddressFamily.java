package io.joss.bgp.net;

import org.apache.commons.lang3.StringUtils;

/**
 * Subsequent address family as defined in RFC 2858
 *
 * @author rainer
 *
 */
public enum SubsequentAddressFamily
{

  NLRI_UNICAST_FORWARDING, NLRI_MULTICAST_FORWARDING, NLRI_UNICAST_MULTICAST_FORWARDING, NLRI_UNICAST_WITH_MPLS_FORWARDING, NLRI_MPLS_LABELLED_VPN;

  private static final String ENCODING_UNICAST_MULTICAST = "Unicast+Multicast";
  private static final String ENCODING_MULTICAST = "Multicast";
  private static final String ENCODING_UNICAST = "Unicast";
  private static final String ENCODING_UNICAST_MPLS = "Unicast-MPLS";
  private static final String ENCODING_MPLS_LABELLED_VPN = "MPLS-Labelled-VPN";

  public int toCode()
  {
	 
    switch (this)
    {
      case NLRI_UNICAST_FORWARDING:
        return 1;
      case NLRI_MULTICAST_FORWARDING:
        return 2;
      case NLRI_UNICAST_MULTICAST_FORWARDING:
        return 3;
      case NLRI_UNICAST_WITH_MPLS_FORWARDING:
        return 4;
      case NLRI_MPLS_LABELLED_VPN:
        return 128;
      default:
        throw new IllegalArgumentException("Unknown subsequent address family: " + this);
    }
  }

  public static SubsequentAddressFamily fromCode(final int code)
  {
    switch (code)
    {
      case 1:
        return NLRI_UNICAST_FORWARDING;
      case 2:
        return NLRI_MULTICAST_FORWARDING;
      case 3:
        return NLRI_UNICAST_MULTICAST_FORWARDING;
      case 4:
        return NLRI_UNICAST_WITH_MPLS_FORWARDING;
      case 128:
        return NLRI_MPLS_LABELLED_VPN;
      default:
        throw new IllegalArgumentException("Unknown subsequent address family code: " + code);
    }
  }

  public static SubsequentAddressFamily fromString(final String value)
  {
    if (StringUtils.equalsIgnoreCase(ENCODING_UNICAST, value) || StringUtils.equalsIgnoreCase("NLRI_UNICAST_FORWARDING", value))
    {
      return NLRI_UNICAST_FORWARDING;
    }
    else if (StringUtils.equalsIgnoreCase(ENCODING_MULTICAST, value) || StringUtils.equalsIgnoreCase("NLRI_MULTICAST_FORWARDING", value))
    {
      return NLRI_MULTICAST_FORWARDING;
    }
    else if (StringUtils.equalsIgnoreCase(ENCODING_UNICAST_MULTICAST, value) || StringUtils.equalsIgnoreCase("NLRI_UNICAST_MULTICAST_FORWARDING", value))
    {
      return NLRI_UNICAST_MULTICAST_FORWARDING;
    }
    else if (StringUtils.equalsIgnoreCase(ENCODING_MPLS_LABELLED_VPN, value) || StringUtils.equalsIgnoreCase("NLRI_MPLS_LABELLED_VPN", value))
    {
      return NLRI_MPLS_LABELLED_VPN;
    }
    else
    {
      throw new IllegalArgumentException("Unknown subsequent address family: " + value);
    }
  }

  @Override
  public String toString()
  {
    switch (this)
    {
      case NLRI_UNICAST_FORWARDING:
        return ENCODING_UNICAST;
      case NLRI_MULTICAST_FORWARDING:
        return ENCODING_MULTICAST;
      case NLRI_UNICAST_MULTICAST_FORWARDING:
        return ENCODING_UNICAST_MULTICAST;
      case NLRI_UNICAST_WITH_MPLS_FORWARDING:
        return ENCODING_UNICAST_MPLS;
      case NLRI_MPLS_LABELLED_VPN:
        return ENCODING_MPLS_LABELLED_VPN;
      default:
        throw new IllegalArgumentException("Unknown subsequent address family: " + this);
    }
  }

}