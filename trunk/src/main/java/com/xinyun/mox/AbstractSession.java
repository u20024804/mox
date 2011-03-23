package com.xinyun.mox;

import java.io.IOException;

public abstract class AbstractSession implements ISession {
	
	NioSession nioSession;

	protected void sendMsg(Object message) {
		nioSession.sendMsg(message);
	}
	
	protected void close() throws IOException {
		nioSession.close();
	}
	
}
