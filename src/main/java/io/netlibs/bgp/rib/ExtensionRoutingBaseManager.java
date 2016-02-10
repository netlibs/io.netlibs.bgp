package io.netlibs.bgp.rib;

public interface ExtensionRoutingBaseManager {

	public abstract PeerRoutingInformationBase extensionRoutingInformationBase(
			String extensionName, String key);

}