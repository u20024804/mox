package com.xinyun.mox.protocol.http;

public class Head {
	
	private final Cookies cookies;
	private final ContentLength contentLength;
	private final Header[] headers;
	
	public Head(final Cookies cookies, final ContentLength contentLength, final Header[] headers) {
		this.cookies = cookies;
		this.contentLength = contentLength;
		this.headers = headers;
	}

	public Cookies getCookies() {
		return cookies;
	}

	public ContentLength getContentLength() {
		return contentLength;
	}

	public Header[] getHeaders() {
		return headers;
	}

}
