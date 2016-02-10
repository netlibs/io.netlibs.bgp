package io.netlibs.bgp.netty.fsm;

public class FireHoldTimerExpired extends FireEventTimeJob {
	public FireHoldTimerExpired() {
		super(FSMEvent.holdTimerExpires());
	}
}