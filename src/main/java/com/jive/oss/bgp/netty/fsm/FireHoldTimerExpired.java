package com.jive.oss.bgp.netty.fsm;

public class FireHoldTimerExpired extends FireEventTimeJob {
	public FireHoldTimerExpired() {
		super(FSMEvent.holdTimerExpires());
	}
}