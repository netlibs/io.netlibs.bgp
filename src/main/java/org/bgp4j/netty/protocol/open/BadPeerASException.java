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
 * File: org.bgp4j.netty.protocol.BadPeerASException.java 
 */
package org.bgp4j.netty.protocol.open;

import org.bgp4j.netty.protocol.NotificationPacket;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class BadPeerASException extends OpenPacketException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6036362844150257520L;

	/**
	 * @param message
	 * @param cause
	 */
	public BadPeerASException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public BadPeerASException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	public BadPeerASException() {
	}

	/**
	 * @param message
	 */
	public BadPeerASException(String message) {
		super(message);
	}

	/* (non-Javadoc)
	 * @see org.bgp4j.netty.protocol.ProtocolPacketFormatException#toNotificationPacket()
	 */
	@Override
	public NotificationPacket toNotificationPacket() {
		return new BadPeerASNotificationPacket();
	}

}
