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
 * File: org.bgp4j.netty.protocol.update.MultiProtocolReachableNLRI.java
 */
package com.jive.oss.bgp.net.attributes;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.jive.oss.bgp.net.AddressFamily;
import com.jive.oss.bgp.net.AddressFamilyKey;
import com.jive.oss.bgp.net.BinaryNextHop;
import com.jive.oss.bgp.net.NetworkLayerReachabilityInformation;
import com.jive.oss.bgp.net.SubsequentAddressFamily;

import lombok.ToString;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@ToString
public class MultiProtocolReachableNLRI extends PathAttribute
{

  private AddressFamily addressFamily;
  private SubsequentAddressFamily subsequentAddressFamily;
  private BinaryNextHop nextHop;
  private List<NetworkLayerReachabilityInformation> nlris = new LinkedList<NetworkLayerReachabilityInformation>();

  /**
   * @param category
   */
  public MultiProtocolReachableNLRI()
  {
    super(Category.OPTIONAL_NON_TRANSITIVE);
  }

  /**
   * @param category
   */
  public MultiProtocolReachableNLRI(final AddressFamily addressFamily, final SubsequentAddressFamily subsequentAddressFamily)
  {
    this();

    this.addressFamily = addressFamily;
    this.subsequentAddressFamily = subsequentAddressFamily;
  }

  /**
   * @param category
   */
  public MultiProtocolReachableNLRI(final AddressFamily addressFamily, final SubsequentAddressFamily subsequentAddressFamily, final byte[] nextHopAddress)
  {
    this(addressFamily, subsequentAddressFamily);

    this.setNextHopAddress(nextHopAddress);
  }

  /**
   * @param category
   */
  public MultiProtocolReachableNLRI(final AddressFamily addressFamily, final SubsequentAddressFamily subsequentAddressFamily, final BinaryNextHop nextHop)
  {
    this(addressFamily, subsequentAddressFamily);

    this.nextHop = nextHop;
  }

  /**
   * @param category
   */
  public MultiProtocolReachableNLRI(final AddressFamily addressFamily, final SubsequentAddressFamily subsequentAddressFamily, final byte[] nextHopAddress,
      final NetworkLayerReachabilityInformation[] nlris)
  {
    this(addressFamily, subsequentAddressFamily, nextHopAddress);

    for (final NetworkLayerReachabilityInformation nlri : nlris)
    {
      this.nlris.add(nlri);
    }
  }

  /**
   * @param category
   */
  public MultiProtocolReachableNLRI(final AddressFamily addressFamily, final SubsequentAddressFamily subsequentAddressFamily, final BinaryNextHop nextHop,
      final NetworkLayerReachabilityInformation[] nlris)
  {
    this(addressFamily, subsequentAddressFamily, nextHop);

    for (final NetworkLayerReachabilityInformation nlri : nlris)
    {
      this.nlris.add(nlri);
    }
  }

  /**
   * @param category
   */
  public MultiProtocolReachableNLRI(final AddressFamily addressFamily, final SubsequentAddressFamily subsequentAddressFamily, final BinaryNextHop nextHop,
      final Collection<NetworkLayerReachabilityInformation> nlris)
  {
    this(addressFamily, subsequentAddressFamily, nextHop);

    if (nlris != null)
    {
      this.nlris.addAll(nlris);
    }
  }

  /**
   * @return the addressFamily
   */
  public AddressFamily getAddressFamily()
  {
    return this.addressFamily;
  }

  /**
   * @param addressFamily
   *          the addressFamily to set
   */
  public void setAddressFamily(final AddressFamily addressFamily)
  {
    this.addressFamily = addressFamily;
  }

  /**
   * @return the subsequentAddressFamily
   */
  public SubsequentAddressFamily getSubsequentAddressFamily()
  {
    return this.subsequentAddressFamily;
  }

  /**
   * @param subsequentAddressFamily
   *          the subsequentAddressFamily to set
   */
  public void setSubsequentAddressFamily(
      final SubsequentAddressFamily subsequentAddressFamily)
  {
    this.subsequentAddressFamily = subsequentAddressFamily;
  }

  /**
   * @return the nextHopAddress
   */
  
  public BinaryNextHop getNextHop()
  {
    return this.nextHop;
  }

  /**
   * @param nextHopAddress
   *          the nextHopAddress to set
   */
  public void setNextHopAddress(final byte[] nextHopAddress)
  {
    if (nextHopAddress != null)
    {
      this.nextHop = new BinaryNextHop(nextHopAddress);
    }
    else
    {
      this.nextHop = null;
    }
  }

  /**
   * @param nextHopAddress
   *          the nextHopAddress to set
   */
  public void setNextHop(final BinaryNextHop nextHop)
  {
    this.nextHop = nextHop;
  }

  /**
   * @return the nlris
   */
  public List<NetworkLayerReachabilityInformation> getNlris()
  {
    return this.nlris;
  }

  /**
   * @param nlris
   *          the nlris to set
   */
  public void setNlris(final List<NetworkLayerReachabilityInformation> nlris)
  {
    this.nlris = nlris;
  }

  public AddressFamilyKey addressFamilyKey()
  {
    return new AddressFamilyKey(this.getAddressFamily(), this.getSubsequentAddressFamily());
  }

  @Override
  protected PathAttributeType internalType()
  {
    return PathAttributeType.MULTI_PROTOCOL_REACHABLE;
  }

  @Override
  protected boolean subclassEquals(final PathAttribute obj)
  {
    final MultiProtocolReachableNLRI o = (MultiProtocolReachableNLRI) obj;

    final EqualsBuilder builer = (new EqualsBuilder())
        .append(this.getAddressFamily(), o.getAddressFamily())
        .append(this.getSubsequentAddressFamily(), o.getSubsequentAddressFamily())
        .append(this.getNextHop(), o.getNextHop())
        .append(this.getNlris().size(), o.getNlris().size());

    if (builer.isEquals())
    {
      final Iterator<NetworkLayerReachabilityInformation> lit = this.getNlris().iterator();
      final Iterator<NetworkLayerReachabilityInformation> rit = o.getNlris().iterator();

      while (lit.hasNext())
      {
        builer.append(lit.next(), rit.next());
      }
    }

    return builer.isEquals();
  }

  @Override
  protected int subclassHashCode()
  {
    final HashCodeBuilder builder = (new HashCodeBuilder())
        .append(this.getAddressFamily())
        .append(this.getSubsequentAddressFamily())
        .append(this.getNextHop());
    final Iterator<NetworkLayerReachabilityInformation> it = this.getNlris().iterator();

    while (it.hasNext())
    {
      builder.append(it.next());
    }

    return builder.toHashCode();
  }

  @Override
  protected int subclassCompareTo(final PathAttribute obj)
  {
    final MultiProtocolReachableNLRI o = (MultiProtocolReachableNLRI) obj;

    final CompareToBuilder builer = (new CompareToBuilder())
        .append(this.getAddressFamily(), o.getAddressFamily())
        .append(this.getSubsequentAddressFamily(), o.getSubsequentAddressFamily())
        .append(this.getNextHop(), o.getNextHop())
        .append(this.getNlris().size(), o.getNlris().size());

    if (builer.toComparison() == 0)
    {
      final Iterator<NetworkLayerReachabilityInformation> lit = this.getNlris().iterator();
      final Iterator<NetworkLayerReachabilityInformation> rit = o.getNlris().iterator();

      while (lit.hasNext())
      {
        builer.append(lit.next(), rit.next());
      }
    }

    return builer.toComparison();
  }

  @Override
  protected ToStringBuilder subclassToString()
  {
    final ToStringBuilder builder = new ToStringBuilder(this)
        .append(this.addressFamily)
        .append(this.subsequentAddressFamily)
        .append(this.nextHop);

    for (final NetworkLayerReachabilityInformation n : this.nlris)
    {
      builder.append("nlri", n);
    }

    return builder;
  }

}
