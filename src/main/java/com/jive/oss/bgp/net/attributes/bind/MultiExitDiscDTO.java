/**
 * 
 */
package com.jive.oss.bgp.net.attributes.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.jive.oss.bgp.net.attributes.MultiExitDiscPathAttribute;


/**
 * @author rainer
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MultiExitDiscDTO  {

	private int discriminator;
	
	public MultiExitDiscDTO() {}
	
	public MultiExitDiscDTO(MultiExitDiscPathAttribute pa) {
		setDiscriminator(pa.getDiscriminator());
	}

	/**
	 * @return the value
	 */
	public int getDiscriminator() {
		return discriminator;
	}

	/**
	 * @param value the value to set
	 */
	public void setDiscriminator(int value) {
		this.discriminator = value;
	}

}
