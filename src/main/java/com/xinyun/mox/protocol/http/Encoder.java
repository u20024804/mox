package com.xinyun.mox.protocol.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xinyun.mox.IBuffer;
import com.xinyun.mox.IEncoder;

public class Encoder implements IEncoder {
	
	private static final Log logger = LogFactory.getLog(Encoder.class);
	
	public static final byte SPACE = ' ';
	public static final byte COLON = ':';
	public static final byte[] NEW_LINE = {'\r', '\n'};

	@Override
	public boolean encoder(IBuffer buffer, Object message) {
		final Response response = (Response) message;
		
		final StatusLine statusLine = response.getStatusLine();
		buffer.write(statusLine.getVersion().getBytes());
		buffer.write1Byte(SPACE);
		buffer.write(String.valueOf(statusLine.getCode()).getBytes());
		buffer.write1Byte(SPACE);
		buffer.write(statusLine.getMessage().getBytes());
		buffer.write(NEW_LINE);
		
		final int contentLength;
		final Head head = response.getHead();
		if(head != null) {
			final Header[] headers = head.getHeaders();
			if(headers != null && headers.length > 0) {
				for (final Header header : headers) {
					buffer.write(header.getKey().getBytes());
					buffer.write1Byte(COLON);
					buffer.write1Byte(SPACE);
					buffer.write(header.getValue().getBytes());
					buffer.write(NEW_LINE);
				}
			}
			
			final Cookies cookies = head.getCookies();
			if(cookies != null) {
				buffer.write(cookies.getKey().getBytes());
				buffer.write1Byte(COLON);
				buffer.write1Byte(SPACE);
				buffer.write(cookies.getValue().getBytes());
				buffer.write(NEW_LINE);
			}
			
			if(head.getContentLength() != null) {
				buffer.write(head.getContentLength().getKey().getBytes());
				buffer.write1Byte(COLON);
				buffer.write1Byte(SPACE);
				buffer.write(head.getContentLength().getValue().getBytes());
				buffer.write(NEW_LINE);
				
				contentLength = head.getContentLength().getContentLength();
			} else {
				contentLength = -1;
			}
		} else {
			contentLength = -1;
		}
		buffer.write(NEW_LINE);
		
		final String body = response.getBody();
		final byte[] bytes;
		if(body == null) {
			logger.info("encode: "+statusLine.getVersion()+" "+statusLine.getCode()+" "+statusLine.getMessage());
			return true;
		} else if(contentLength == 0) {
			if(body != null && body.length() != 0) {
				throw new EncoderException("contentLength="+contentLength+", but body size: "+body.length());
			}
			logger.info("encode: "+statusLine.getVersion()+" "+statusLine.getCode()+" "+statusLine.getMessage());
			return true;
		} else if(contentLength > 0) {
			bytes = body.getBytes();
			if(contentLength != bytes.length) {
				throw new EncoderException("contentLength="+contentLength+", but body size: "+body.length());
			}
			if(buffer.writeable() < contentLength) {
				return false;
			}
		} else {
			bytes = body.getBytes();
		}
		
		buffer.write(bytes);
		
		logger.info("encode: "+statusLine.getVersion()+" "+statusLine.getCode()+" "+statusLine.getMessage()+"\n\t"+"body size: "+bytes.length);
		return true;
	}

}
