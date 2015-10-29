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
 * File: org.bgp4j.rib.RoutingInformationBaseKey.java
 */
package com.jive.oss.bgp.net;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import lombok.ToString;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@ToString
public class AddressFamilyKey implements Comparable<AddressFamilyKey>
{

  public static final AddressFamilyKey IPV4_UNICAST_FORWARDING = new AddressFamilyKey(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING);
  public static final AddressFamilyKey IPV6_UNICAST_FORWARDING = new AddressFamilyKey(AddressFamily.IPv6, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING);
  public static final AddressFamilyKey IPV4_UNICAST_MPLS_FORWARDING = new AddressFamilyKey(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_WITH_MPLS_FORWARDING);
  public static final AddressFamilyKey IPV6_UNICAST_MPLS_FORWARDING = new AddressFamilyKey(AddressFamily.IPv6, SubsequentAddressFamily.NLRI_UNICAST_WITH_MPLS_FORWARDING);
  public static final AddressFamilyKey IPV4_MPLS_VPN_FORWARDING  = new AddressFamilyKey(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_MPLS_LABELLED_VPN);
  public static final AddressFamilyKey IPV6_MPLS_VPN_FORWARDING = new AddressFamilyKey(AddressFamily.IPv6, SubsequentAddressFamily.NLRI_MPLS_LABELLED_VPN);
  
  private final AddressFamily addressFamily;
  private final SubsequentAddressFamily subsequentAddressFamily;

  public AddressFamilyKey(final AddressFamily addressFamily, final SubsequentAddressFamily subsequentAddressFamily)
  {
    this.addressFamily = addressFamily;
    this.subsequentAddressFamily = subsequentAddressFamily;
  }

  /**
   * @return the addressFamily
   */
  public AddressFamily getAddressFamily()
  {
    return this.addressFamily;
  }

  /**
   * @return the subsequentAddressFamily
   */
  public SubsequentAddressFamily getSubsequentAddressFamily()
  {
    return this.subsequentAddressFamily;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    return (new HashCodeBuilder()).append(this.addressFamily).append(this.subsequentAddressFamily).toHashCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj)
  {
    if (!(obj instanceof AddressFamilyKey))
    {
      return false;
    }

    final AddressFamilyKey o = (AddressFamilyKey) obj;

    return (new EqualsBuilder())
        .append(this.addressFamily, o.addressFamily)
        .append(this.subsequentAddressFamily, o.subsequentAddressFamily)
        .isEquals();
  }

  @Override
  public int compareTo(final AddressFamilyKey o)
  {
    return (new CompareToBuilder())
        .append(this.addressFamily, o.addressFamily)
        .append(this.subsequentAddressFamily, o.subsequentAddressFamily)
        .toComparison();
  }

  public boolean matches(final AddressFamily afi, final SubsequentAddressFamily safi)
  {
    return ((this.getAddressFamily() == afi) && (this.getSubsequentAddressFamily() == safi));
  }
}
