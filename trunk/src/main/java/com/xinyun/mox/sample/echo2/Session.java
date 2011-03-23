package com.xinyun.mox.sample.echo2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xinyun.mox.AbstractSession;

public class Session extends AbstractSession {
	
	private static final Log logger = LogFactory.getLog(Session.class);
	
	private final String name;
	
	public Session(final String name) {
		this.name = name;
	}

	@Override
	public void create() {
		logger.warn(name+" create");
	}

	@Override
	public void accepted() {
		logger.warn(name+" accepted");
	}

	@Override
	public void closed() {
		logger.warn(name+" close");
	}

	@Override
	public void closedByPeer() {
		logger.warn(name+" closedByPeer");
	}

	@Override
	public void receivedMsg(Object message) {
		Byte b = (Byte) message;
		this.sendMsg(b);
	}

}
