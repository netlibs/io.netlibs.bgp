package io.netlibs.bgp.netty.fsm;

public class FireDelayOpenTimerExpired extends FireEventTimeJob {
	public FireDelayOpenTimerExpired() {
		super(FSMEvent.delayOpenTimerExpires());
	}
}