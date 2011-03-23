package com.xinyun.mox.sample.echo;

import java.util.concurrent.atomic.AtomicInteger;

import com.xinyun.mox.ISession;
import com.xinyun.mox.ISessionFactory;

public class SessionFactory implements ISessionFactory {
	
	private final AtomicInteger seq = new AtomicInteger();

	@Override
	public ISession create() {
		return new Session("EchoSession@"+seq.getAndIncrement());
	}

}
