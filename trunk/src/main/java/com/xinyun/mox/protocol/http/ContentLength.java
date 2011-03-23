package com.xinyun.mox.protocol.http;

public class ContentLength extends Header {
	
	public static final String KEY = "Content-length";
	
	private final int contentLength;
	
	public ContentLength(final String value) {
		super(KEY, value);
		
		contentLength = Integer.parseInt(value);
	}
	public ContentLength(final int value) {
		this(String.valueOf(value));
	}
	
	@Override
	public String getKey() {
		return KEY;
	}

	public int getContentLength() {
		return contentLength;
	}

}
