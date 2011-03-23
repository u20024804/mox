package com.xinyun.mox.sample.echo;

import java.util.concurrent.Executor;

import com.xinyun.mox.IDecoderFactory;
import com.xinyun.mox.IEncoderFactory;
import com.xinyun.mox.INioService;
import com.xinyun.mox.ISessionFactory;

public class EchoService implements INioService {
	
	private final ISessionFactory sessionFactory = new SessionFactory();
	private final IDecoderFactory decoderFactory = new DecoderFactory();
	private final IEncoderFactory encoderFactory = new EncoderFactory();

	@Override
	public ISessionFactory getSessionFactory() {
		return sessionFactory;
	}

	@Override
	public IDecoderFactory getDecoderFactory() {
		return decoderFactory;
	}

	@Override
	public IEncoderFactory getEncoderFactory() {
		return encoderFactory;
	}

	@Override
	public Executor getExecutor() {
		// TODO Auto-generated method stub
		return null;
	}

}
