package io.netlibs.bgp.netty.fsm;

public class FireSendKeepalive extends FireEventTimeJob {
	public FireSendKeepalive() {
		super(FSMEvent.keepaliveTimerExpires());
	}
}