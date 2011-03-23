package com.xinyun.mox;

public interface ISession {
	
	void create();
	void accepted();
	void closed();
	void closedByPeer();
	void receivedMsg(Object message);

}
