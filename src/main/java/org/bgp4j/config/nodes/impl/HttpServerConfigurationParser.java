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
 * File: org.bgp4.config.impl.BgpServerConfigurationParser.java 
 */
package org.bgp4j.config.nodes.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.bgp4j.config.nodes.HttpServerConfiguration;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */
@Singleton
public class HttpServerConfigurationParser {

	private @Inject ServerConfigurationParser serverConfigurationParser;
	
	public HttpServerConfiguration parseConfiguration(HierarchicalConfiguration config) throws ConfigurationException {
		HttpServerConfigurationImpl result = new HttpServerConfigurationImpl();
		List<HierarchicalConfiguration> serverConfig = config.configurationsAt("Server");
		
		if(serverConfig.size() == 1)
			result.setServerConfiguration(serverConfigurationParser.parseConfig(serverConfig.get(0)));
		else if(serverConfig.size() > 1)
			throw new ConfigurationException("duplicate <Server/> element");
		else
			result.setServerConfiguration(new ServerConfigurationImpl());
		
		return result;
		
	}
}
