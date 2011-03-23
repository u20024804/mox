package com.xinyun.mox.protocol.http;

public class SessionId extends Cookie {
	
	public static final String SESSIONID = "JSESSIONID";
	
	public SessionId(final String key, final String value) {
		super(key, value);
	}

}
