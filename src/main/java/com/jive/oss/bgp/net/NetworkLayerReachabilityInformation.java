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
 */
package com.jive.oss.bgp.net;

import java.io.Serializable;
import java.net.InetAddress;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This class models the generic network layer reachabibility information as defined in RFC 4271 and RFC 2858.
 *
 * The Network Layer Reachability Information (NLRI) is a variable length bit field.
 *
 * On the network layer it is encondig as a leading length (1 octet) and a varaiable number of of octets each carrying an 8-bit part of the
 * NLRI. The rightmost octet is filled up with trailing bits which a re ignored by the receiver and should be set to 0 by the sender. If the
 * prefix length is zero then the number of trailing NLRI octets is zero as well.
 *
 * Then number of octets need to carry the NLRI can be calculated from this formula: number of octets = (prefix length / 8) + (prefix length
 * % 8 > 0 ? 1 : 0)
 *
 * NB: This class is also used to model the IPv4 withdrawn routes information in UPDATE (type 2) packets.
 *
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

public class NetworkLayerReachabilityInformation implements Serializable, Comparable<NetworkLayerReachabilityInformation>
{

  /**
   *
   */
  private static final long serialVersionUID = -8319262066302848737L;

  private int prefixLength;
  private byte[] prefix;


  public NetworkLayerReachabilityInformation()
  {
  }

  public NetworkLayerReachabilityInformation(final byte[] data)
  {
    this.prefixLength = data.length;
    this.prefix = data;
  }

  public NetworkLayerReachabilityInformation(final int prefixLength, final byte[] prefix)
  {
    this.setPrefix(prefixLength, prefix);
  }

  public NetworkLayerReachabilityInformation(final InetAddress source)
  {
    final byte[] raw = source.getAddress();
    this.setPrefix(8 * raw.length, raw);
  }


  /**
   * @return the prefixLength
   */
  public int getPrefixLength()
  {
    return this.prefixLength;
  }

  /**
   * @return the prefix
   */
  public byte[] getPrefix()
  {
    return this.prefix;
  }

  /**
   * set the prefix length and value in one step. The prefix value length in octets is checked against the prefix length and the number of
   * octets calculated from the prefix length. The trailing bits are masked out to 0.
   *
   * @param prefixLength
   * @param prefix
   */
  public void setPrefix(final int prefixLength, final byte[] prefix)
  {
    this.prefixLength = prefixLength;
    this.prefix = prefix;

    if (prefix == null)
    {
      if (prefixLength != 0)
      {
        throw new IllegalArgumentException("cannot set null prefix if prefix length greater 0");
      }
      this.prefix = new byte[] { 0 };
    }
    else if (prefixLength == 0)
    {
      this.prefix = new byte[] { 0 };
    }
    else
    {
      final int prefixSize = calculateOctetsForPrefixLength(this.prefixLength);

      if (prefix.length != prefixSize)
      {
        throw new IllegalArgumentException("expected a prefix with " + prefixSize + " octets but got " + prefix.length + " octets");
      }

      // skip masking unless prefix length > 0
      if (prefixLength > 0)
      {
        // mask out trailing bits
        final int trailingBits = ((8 * prefixSize) - prefixLength);

        if (trailingBits > 0)
        {
          for (int bit = 0; bit < trailingBits; bit++)
          {
            prefix[prefixSize - 1] &= ~(1 << bit);
          }
        }
      }
    }
  }

  /**
   * check if this instance if a prefix of the given other prefix. The criteria for being a prefix are: - The prefix length of the other
   * NLRI is longer than this NLRI length - The prefix bits match up to the shorter prefix length
   *
   * If the other NLRI equals this NLRI then this NLRI is NOT a prefix of the other NLRI. If this NLRI has a zero-length prefix length it is
   * considered to be a prefix of any other NLRI except the other NLRI also has a zero-length prefix length
   *
   * @param other
   * @return
   */
  public boolean isPrefixOf(final NetworkLayerReachabilityInformation other)
  {
    boolean isPrefix = false;

    if (this.prefixLength > 0)
    {
      if ((other.prefixLength > 0)
          && (other.prefixLength > this.prefixLength))
      {
        final int byteLength = calculateOctetsForPrefixLength(this.prefixLength);
        boolean match = true;

        // test the full prefix octets
        for (int i = 0; i < (byteLength - 1); i++)
        {
          if (this.prefix[i] != other.prefix[i])
          {
            match = false;
            break;
          }
        }

        // prefix octets match, check remaining bits
        if (match)
        {
          final int bitsToCheck = this.prefixLength % 8;

          if (bitsToCheck == 0)
          {
            match = (this.prefix[byteLength - 1] == other.prefix[byteLength - 1]);
          }
          else
          {
            for (int i = 0; i < bitsToCheck; i++)
            {
              final int mask = 1 << (7 - i);

              if ((this.prefix[byteLength - 1] & mask) != (other.prefix[byteLength - 1] & mask))
              {
                match = false;
                break;
              }
            }
          }
        }

        isPrefix = match;
      }
    }
    else
    {
      isPrefix = (other.prefixLength > 0);
    }

    return isPrefix;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    return (new HashCodeBuilder())
        .append(this.getPrefixLength())
        .append(this.getPrefix())
        .toHashCode();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj)
  {
    if (!(obj instanceof NetworkLayerReachabilityInformation))
    {
      return false;
    }

    final NetworkLayerReachabilityInformation o = (NetworkLayerReachabilityInformation) obj;

    return (new EqualsBuilder())
        .append(this.getPrefixLength(), o.getPrefixLength())
        .append(this.getPrefix(), o.getPrefix())
        .isEquals();
  }

  @Override
  public int compareTo(final NetworkLayerReachabilityInformation other)
  {
    final CompareToBuilder builder = new CompareToBuilder();

    builder.append(this.getPrefixLength(), other.getPrefixLength());

    if (builder.toComparison() == 0)
    {
      final int byteLen = calculateOctetsForPrefixLength(this.prefixLength);
      final int otherByteLen = calculateOctetsForPrefixLength(other.prefixLength);
      final int commonByteLen = (byteLen > otherByteLen) ? otherByteLen : byteLen;
      final int commonPrefixLen = (this.prefixLength > other.prefixLength) ? other.prefixLength : this.prefixLength;

      for (int i = 0; i < (commonByteLen - 1); i++)
      {
        builder.append(this.getPrefix()[i], other.getPrefix()[i]);
      }

      if (builder.toComparison() == 0)
      {
        final int bitsToCheck = commonPrefixLen % 8;

        if (bitsToCheck == 0)
        {
          if (commonByteLen > 0)
          {
            builder.append(this.getPrefix()[commonByteLen - 1], other.getPrefix()[commonByteLen - 1]);
          }
        }
        else
        {
          for (int i = 0; i < bitsToCheck; i++)
          {
            final int mask = 1 << (7 - i);

            builder.append(this.getPrefix()[commonByteLen - 1] & mask, other.getPrefix()[commonByteLen - 1] & mask);
            if (builder.toComparison() != 0)
            {
              break;
            }
          }
        }

        if (builder.toComparison() == 0)
        {
          builder.append(this.getPrefixLength(), other.getPrefixLength());
        }
      }

    }

    return builder.toComparison();
  }

  public static final int calculateOctetsForPrefixLength(final int prefixLength)
  {
    return (prefixLength / 8) + ((prefixLength % 8) > 0 ? 1 : 0);
  }

  private static final char[] chars = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();

    for (final byte element : this.prefix)
    {
      builder.append(chars[(element / 16) & 0x0f]);
      builder.append(chars[(element % 16) & 0x0f]);
    }
    builder.append('/');
    builder.append(Integer.toString(this.prefixLength));

    return builder.toString();
  }

}
