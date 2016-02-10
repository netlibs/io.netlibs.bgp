package io.joss.bgp.rib;

public interface ExtensionRoutingBaseManager {

	public abstract PeerRoutingInformationBase extensionRoutingInformationBase(
			String extensionName, String key);

}