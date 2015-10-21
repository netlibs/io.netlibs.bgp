package com.jive.oss.bgp.netty.fsm;

public class FireIdleHoldTimerExpired extends FireEventTimeJob {
	public FireIdleHoldTimerExpired() {
		super(FSMEvent.idleHoldTimerExpires());
	}
}