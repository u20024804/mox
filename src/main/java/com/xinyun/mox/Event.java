package com.xinyun.mox;

public class Event {
	
	private final EventType type;
	private final NioSession session;
	private final Object message;
	
	public Event(final EventType type, final NioSession session, final Object message) {
		this.type = type;
		this.session = session;
		this.message = message;
	}

	public EventType getType() {
		return type;
	}

	public NioSession getSession() {
		return session;
	}

	public Object getMessage() {
		return message;
	}

}
