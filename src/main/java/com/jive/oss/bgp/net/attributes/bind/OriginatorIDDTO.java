/**
 * 
 */
package com.jive.oss.bgp.net.attributes.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.jive.oss.bgp.net.attributes.OriginatorIDPathAttribute;


/**
 * @author rainer
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OriginatorIDDTO  {

	private int value;
	
	public OriginatorIDDTO() {}
	
	public OriginatorIDDTO(OriginatorIDPathAttribute pa) {
		setValue(pa.getOriginatorID());
	}

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(int value) {
		this.value = value;
	}

}
