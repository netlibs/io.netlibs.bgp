/**
 * 
 */
package com.jive.oss.bgp.net.attributes.bind;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.jive.oss.bgp.net.AddressFamily;
import com.jive.oss.bgp.net.NetworkLayerReachabilityInformation;
import com.jive.oss.bgp.net.SubsequentAddressFamily;
import com.jive.oss.bgp.net.attributes.MultiProtocolUnreachableNLRI;

/**
 * @author rainer
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MultiProtocolUnreachableDTO {
	private AddressFamily addressFamily;
	private SubsequentAddressFamily subsequentAddressFamily;
	private List<NetworkLayerReachabilityInformation> nlris = new LinkedList<NetworkLayerReachabilityInformation>();
	
	public MultiProtocolUnreachableDTO() {}
	
	public MultiProtocolUnreachableDTO(MultiProtocolUnreachableNLRI pa) {
		setAddressFamily(pa.getAddressFamily());
		setSubsequentAddressFamily(pa.getSubsequentAddressFamily());
		setNlris(pa.getNlris());
	}
	
	/**
	 * @return the addressFamily
	 */
	public AddressFamily getAddressFamily() {
		return addressFamily;
	}
	/**
	 * @param addressFamily the addressFamily to set
	 */
	public void setAddressFamily(AddressFamily addressFamily) {
		this.addressFamily = addressFamily;
	}
	/**
	 * @return the subsequentAddressFamily
	 */
	public SubsequentAddressFamily getSubsequentAddressFamily() {
		return subsequentAddressFamily;
	}
	/**
	 * @param subsequentAddressFamily the subsequentAddressFamily to set
	 */
	public void setSubsequentAddressFamily(
			SubsequentAddressFamily subsequentAddressFamily) {
		this.subsequentAddressFamily = subsequentAddressFamily;
	}
	/**
	 * @return the nlris
	 */
	public List<NetworkLayerReachabilityInformation> getNlris() {
		return nlris;
	}
	/**
	 * @param nlris the nlris to set
	 */
	public void setNlris(List<NetworkLayerReachabilityInformation> nlris) {
		this.nlris = nlris;
	}
}
