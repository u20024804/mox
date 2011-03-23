package com.xinyun.mox;

import java.util.concurrent.Executor;

public interface INioService {
	
	ISessionFactory getSessionFactory();
	IDecoderFactory getDecoderFactory();
	IEncoderFactory getEncoderFactory();
	Executor getExecutor();

}
