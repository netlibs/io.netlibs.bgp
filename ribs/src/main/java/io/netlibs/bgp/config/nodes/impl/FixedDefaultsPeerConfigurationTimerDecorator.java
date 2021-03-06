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
 * File: org.bgp4.config.nodes.impl.FixedDefaultsPeerConfigurationTimerDecorator.java 
 */
package io.netlibs.bgp.config.nodes.impl;

import io.netlibs.bgp.config.nodes.PeerConfiguration;
import io.netlibs.bgp.config.nodes.PeerConfigurationTimerDecorator;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public class FixedDefaultsPeerConfigurationTimerDecorator extends PeerConfigurationTimerDecorator {

	public FixedDefaultsPeerConfigurationTimerDecorator(PeerConfiguration decorated) {
		super(decorated);
	}

	/* (non-Javadoc)
	 * @see org.bgp4.config.nodes.PeerConfigurationTimerDecorator#getDefaultHoldTime()
	 */
	@Override
	protected int getDefaultHoldTime() {
		return 120;
	}

	/* (non-Javadoc)
	 * @see org.bgp4.config.nodes.PeerConfigurationTimerDecorator#getDefaultConnectRetryInterval()
	 */
	@Override
	protected int getDefaultIdleHoldTime() {
		return 30;
	}

	@Override
	protected int getDefaultDelayOpenTime() {
		return 15;
	}

	@Override
	protected int getDefaultConnectRetryTime() {
		return 60;
	}

	@Override
	protected int getDefaultAutomaticStartInterval() {
		return 120;
	}

}
