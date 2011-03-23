package com.xinyun.mox.protocol.http;

import java.util.concurrent.Executor;

import com.xinyun.mox.IDecoderFactory;
import com.xinyun.mox.IEncoderFactory;
import com.xinyun.mox.INioService;
import com.xinyun.mox.ISessionFactory;

public class HttpService implements INioService {

	@Override
	public ISessionFactory getSessionFactory() {
		return new SessionFactory();
	}

	@Override
	public IDecoderFactory getDecoderFactory() {
		return new DecoderFactory();
	}

	@Override
	public IEncoderFactory getEncoderFactory() {
		return new EncoderFactory();
	}

	@Override
	public Executor getExecutor() {
		return null;
	}

}
