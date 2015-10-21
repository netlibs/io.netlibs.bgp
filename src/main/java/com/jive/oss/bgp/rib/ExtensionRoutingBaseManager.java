package com.jive.oss.bgp.rib;

public interface ExtensionRoutingBaseManager {

	public abstract PeerRoutingInformationBase extensionRoutingInformationBase(
			String extensionName, String key);

}