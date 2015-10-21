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
package org.bgp4j.netty.protocol.update;

import org.bgp4j.netty.protocol.NotificationPacket;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class InvalidNextHopException extends AttributeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8535948955861003621L;

	/**
	 * 
	 */
	public InvalidNextHopException() {
	}

	/**
	 * @param offendingAttribute
	 */
	public InvalidNextHopException(byte[] offendingAttribute) {
		super(offendingAttribute);
	}

	/**
	 * @param message
	 * @param offendingAttribute
	 */
	public InvalidNextHopException(String message, byte[] offendingAttribute) {
		super(message, offendingAttribute);
	}

	@Override
	public NotificationPacket toNotificationPacket() {
		return new InvalidNextHopNotificationPacket(getOffendingAttribute());
	}

}
