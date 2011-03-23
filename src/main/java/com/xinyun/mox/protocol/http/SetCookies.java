package com.xinyun.mox.protocol.http;

public class SetCookies extends Header {
	
	public static final String KEY = "Set-Cookie";
	
	public SetCookies(final String value) {
		super(KEY, value);
	}
	
	@Override
	public String getKey() {
		return KEY;
	}

}
