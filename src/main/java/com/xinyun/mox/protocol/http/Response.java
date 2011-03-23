package com.xinyun.mox.protocol.http;

import com.xinyun.mox.protocol.IResponse;

public class Response implements IResponse {
	
	private final StatusLine statusLine;
	private final Head head;
	private final String body;
	
	public Response(final StatusLine statusLine, final Head head, final String body) {
		this.statusLine = statusLine;
		this.head = head;
		this.body = body;
	}

	public StatusLine getStatusLine() {
		return statusLine;
	}

	public Head getHead() {
		return head;
	}

	public String getBody() {
		return body;
	}

}
