package com.xinyun.mox.protocol.http;

public class OKResponse extends Response {
	
	public OKResponse(final Head head, final String body) {
		super(StatusLine.OK, head, body);
	}

}
