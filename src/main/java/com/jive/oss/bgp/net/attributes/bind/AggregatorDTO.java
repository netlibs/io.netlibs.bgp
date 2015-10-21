/**
 * 
 */
package com.jive.oss.bgp.net.attributes.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.jive.oss.bgp.net.ASType;
import com.jive.oss.bgp.net.attributes.AggregatorPathAttribute;

/**
 * @author rainer
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AggregatorDTO {
	private ASType asType;
	private int asNumber;
	private byte[] aggregatorAddress;
	
	public AggregatorDTO() {}
	
	public AggregatorDTO(AggregatorPathAttribute pa) {
		setAsNumber(pa.getAsNumber());
		setAsType(pa.getAsType());
		setAggregatorAddress(pa.getAggregator().getAddress());
	}

	/**
	 * @return the asType
	 */
	public ASType getAsType() {
		return asType;
	}

	/**
	 * @param asType the asType to set
	 */
	public void setAsType(ASType asType) {
		this.asType = asType;
	}

	/**
	 * @return the asNumber
	 */
	public int getAsNumber() {
		return asNumber;
	}

	/**
	 * @param asNumber the asNumber to set
	 */
	public void setAsNumber(int asNumber) {
		this.asNumber = asNumber;
	}

	/**
	 * @return the aggregatorAddress
	 */
	public byte[] getAggregatorAddress() {
		return aggregatorAddress;
	}

	/**
	 * @param aggregatorAddress the aggregatorAddress to set
	 */
	public void setAggregatorAddress(byte[] aggregatorAddress) {
		this.aggregatorAddress = aggregatorAddress;
	}
}
