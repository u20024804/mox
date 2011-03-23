package com.xinyun.mox.protocol.http;

public class StatusLine {
	
	public static final String VERSION = "HTTP/1.1";
	
	public static final StatusLine OK;
	public static final StatusLine NOT_FOUND;
	public static final StatusLine INTERNAL_SERVER_ERROR;
	
	static {
		OK = new StatusLine(200, "OK");
		NOT_FOUND = new StatusLine(404, "Not Found");
		INTERNAL_SERVER_ERROR = new StatusLine(500, "Internal Server Error");
	}
	
	private final int code;
	private final String message;
	private final String version;
	
	public StatusLine(final int code, final String message, final String version) {
		this.code = code;
		this.message = message;
		this.version = version;
	}
	public StatusLine(final int code, final String message) {
		this(code, message, VERSION);
	}
	
	public int getCode() {
		return code;
	}
	public String getMessage() {
		return message;
	}

	public String getVersion() {
		return version;
	}

}
