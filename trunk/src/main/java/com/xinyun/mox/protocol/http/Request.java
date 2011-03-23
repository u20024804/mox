package com.xinyun.mox.protocol.http;

import com.xinyun.mox.protocol.IRequest;

public class Request implements IRequest {
	
	private final Method method;
	private final Head head;
	private final String body;
	
	public Request(final Method method, final Head head, final String body) {
		this.method = method;
		this.head = head;
		this.body = body;
	}
	
	public Method getMethod() {
		return method;
	}
	public Head getHead() {
		return head;
	}
	public String getBody() {
		return body;
	}

}
