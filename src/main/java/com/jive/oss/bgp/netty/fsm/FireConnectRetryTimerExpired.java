package com.jive.oss.bgp.netty.fsm;

public class FireConnectRetryTimerExpired extends FireEventTimeJob {
	public FireConnectRetryTimerExpired() {
		super(FSMEvent.connectRetryTimerExpires());
	}
}