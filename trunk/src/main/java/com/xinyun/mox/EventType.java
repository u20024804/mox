package com.xinyun.mox;

public enum EventType {
	
	CREATE(1 << 1),
	ACCEPT(1 << 2),
	READ(1 << 3),
	CLOSED(1 << 4),
	CLOSED_BY_PEER(1 << 5);
	
	
	private final int value;
	
	EventType(final int value) {
		this.value = value;
	}
	
	 public int value() { return value; }

}
