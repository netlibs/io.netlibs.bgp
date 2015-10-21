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
 * File: org.bgp4j.rib.RoutingInformationBaseVisitor.java 
 */
package org.bgp4j.rib;

import java.util.UUID;

import org.bgp4j.net.AddressFamilyKey;
import org.bgp4j.net.RIBSide;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
public interface PeerRoutingInformationBaseVisitor {

	/**
	 * Called when a routing base is visited
	 * 
	 * @param ribName
	 * @param afk
	 * @param side
	 */
	void visitRoutingBase(String ribName, UUID ribID, AddressFamilyKey afk, RIBSide side);

}
