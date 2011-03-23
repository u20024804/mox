package com.xinyun.mox.protocol.http;

import java.io.IOException;

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
		final Request request = (Request) message;
		final Method method = request.getMethod();
		logger.debug("received request: "+method.getMethod()+" "+method.getUrl()+" "+method.getVersion());
		
		final String body = "hello world!\r\n";
		final ContentLength contentLength = new ContentLength(body.length()); //对于非ascii字符串，应该用body.getBytes().length
		final Head head = new Head(null, contentLength, null);
		
		final StatusLine statusLine = new StatusLine(200, "OK", request.getMethod().getVersion());
		final Response response = new Response(statusLine, head, body);
		this.sendMsg(response);
		try {
			this.close();
		} catch (IOException e) {
			logger.error("close exception", e);
		}
	}

}
