package com.jive.oss.bgp.net;

import java.net.InetAddress;
import java.util.Comparator;

public final class InetAddressComparator implements Comparator<InetAddress>
{
  @Override
  public int compare(final InetAddress l, final InetAddress r)
  {

    final byte[] la = l.getAddress();
    final byte[] ra = r.getAddress();

    if (la.length < ra.length)
    {
      return -1;
    }
    else if (la.length > ra.length)
    {
      return 1;
    }
    else
    {
      for (int i = 0; i < la.length; i++)
      {
        if (la[i] < ra[i])
        {
          return -1;
        }
        else if (la[i] > ra[i])
        {
          return 1;
        }
      }
      return 0;
    }

  }

}