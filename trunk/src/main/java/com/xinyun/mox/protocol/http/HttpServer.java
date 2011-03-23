package com.xinyun.mox.protocol.http;

import java.io.IOException;

import com.xinyun.mox.INioService;
import com.xinyun.mox.NioWorker;

public class HttpServer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		final INioService httpService = new HttpService();
		final NioWorker worker = new NioWorker(httpService);
		worker.setHost("127.0.0.1");
		worker.setPort(8877);
		worker.run();
	}

}
