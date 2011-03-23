package com.xinyun.mox.protocol.http;

import java.util.ArrayList;

public class Cookies extends Header {
	
	public static final String KEY = "Cookie";
	
	private final Cookie[] cookies;
	private final SessionId session;
	
	public Cookies(final String cookieContent) {
		super(KEY, cookieContent);
		
		final String[] items = cookieContent.split(";");
		final ArrayList<Cookie> cookieArray = new ArrayList<Cookie>(items.length);
		
		SessionId localSession = null;
		
		for(final String item : items) {
			final String[] parts = item.split("=", 2);
			if(parts.length != 2) {
				throw new DecoderException("parse cookie exception: '"+item+"'");
			}
			final String key = parts[0];
			final String value = parts[1];
			
			if(key.equals(SessionId.SESSIONID)) {
				if(localSession != null) {
					throw new DecoderException("duplicate sessionId defined: "+cookieContent);
				}
				localSession = new SessionId(key, value);
			} else {
				cookieArray.add(new Cookie(key, value));
			}
		}
		session = localSession;
		cookies = cookieArray.toArray(new Cookie[0]);
	}

	@Override
	public String getKey() {
		return KEY;
	}

	public Cookie[] getCookies() {
		return cookies;
	}

	public SessionId getSession() {
		return session;
	}

}
