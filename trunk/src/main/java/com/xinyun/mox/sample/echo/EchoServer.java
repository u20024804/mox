package com.xinyun.mox.sample.echo;

import java.io.IOException;

import com.xinyun.mox.INioService;
import com.xinyun.mox.NioWorker;

public class EchoServer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		final INioService nioService = new EchoService();
		NioWorker nioWorker = new NioWorker(nioService);
		nioWorker.setHost("127.0.0.1");
		nioWorker.setPort(8912);
		nioWorker.run();
	}

}
