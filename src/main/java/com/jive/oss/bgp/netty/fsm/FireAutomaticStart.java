package com.jive.oss.bgp.netty.fsm;

public class FireAutomaticStart extends FireEventTimeJob {
	public FireAutomaticStart() {
		super(FSMEvent.automaticStart());
	}
}