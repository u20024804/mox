package com.xinyun.mox.protocol.http;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xinyun.mox.IBuffer;
import com.xinyun.mox.IDecoder;

public class Decoder implements IDecoder {
	
	private static final Log logger = LogFactory.getLog(Decoder.class);
	
	@Override
	public Request decode(IBuffer buffer) {
		final Method method = decodeMethod(buffer);
		if(method == null) {
			return null;
		}
		
		final Head head = decodeHead(buffer);
		if (head == null) {
			return null;
		}
		
		final String body;
		if(head.getContentLength() != null && head.getContentLength().getContentLength() > 0) {
			body = decodeBody(buffer, head.getContentLength().getContentLength());
		} else {
			body = null;
		}
		
		logger.info("decode: "+method.getMethod()+" "+method.getUrl()+" "+method.getVersion());
		return new Request(method, head, body);
	}
	
	private String readLine(IBuffer buffer) {
		final int readable = buffer.readable();
		int readed;
		for(readed = 0; readed < readable && buffer.read1Byte() != '\r'; ++readed);
		if(readed == readable) {
			return null;
		} if(readed > readable) {
			throw new AssertionError();
		}
		
		if(buffer.read1Byte() != '\n') {
			throw new DecoderException("found \\r but not found \\n");
		}
		
		final byte[] bs = new byte[readed];
		buffer.moveBackR(readed + 2);
		
		buffer.read(bs);
		buffer.read2Bytes();
		
		if(readed == 0) {
			return "";
		}
		final String line = new String(bs);
		return line;
	}
	
	public Method decodeMethod(IBuffer buffer) {
		final String methodStr = readLine(buffer);
		if(methodStr == null) {
			return null;
		}
		
		final Method method;
		if(methodStr.startsWith(GetMethod.METHOD)) {
			method = new GetMethod();
		} else if(methodStr.startsWith(PostMethod.METHOD)) {
			method = new PostMethod();
		} else {
			throw new DecoderException("unsupport http method: "+methodStr);
		}
		
		final String[] parts = methodStr.substring(method.getMethod().length()).trim().split(" ");
		if(parts.length < 2) {
			throw new DecoderException("can't parse method line: "+methodStr);
		}

		final String url = parts[0].trim();
		final String version = parts[parts.length - 1].trim();
		if(!version.startsWith("HTTP/")) {
			throw new DecoderException("http version must be start with \"HTTP/\"");
		}
		
		final String path;
		final String args;
		
		final int argsBorder = url.indexOf('?');
		if(argsBorder < 0) {
			path = url;
			args = null;
		} else {
			path = url.substring(0, argsBorder);
			args = url.substring(argsBorder + 1);
		}
		
		method.setArgs(args);
		method.setPath(path);
		method.setUrl(url);
		method.setVersion(version);
		return method;
	}

	public Head decodeHead(IBuffer buffer) {
		final ArrayList<Header> headers = new ArrayList<Header>(8);
		Cookies cookies = null;
		ContentLength contentLength = null;
		
		while(true) {
			final String line = readLine(buffer);
			if(line == null) {
				return null;
			} else if(line.length() == 0) {
				break;
			}
			final Header header = decodeHeader(line);
			if(header instanceof Cookies) {
				if(cookies != null) {
					throw new DecoderException("duplicate Cookie defined: old: "+cookies.getValue()+", new: "+header.getValue());
				}
				cookies = (Cookies) header;
			} else if(header instanceof ContentLength) {
				if(contentLength != null) {
					throw new DecoderException("duplicate ContentLength defined: old: "+contentLength.getValue()+", new: "+header.getValue());
				}
				contentLength = (ContentLength) header;
			} else {
				headers.add(header);
			}
		}
		
		return new Head(cookies, contentLength, headers.toArray(new Header[0]));
	}
	
	public Header decodeHeader(final String line) {
		final String[] parts = line.split(":", 2);
		if(parts.length != 2) {
			throw new DecoderException("parse header error: '"+line+"'");
		}
		final String key = parts[0].trim();
		final String value = parts[1].trim();
		
		final Header header;
		if(key.equals(Cookies.KEY)) {
			header = new Cookies(value);
		} else if(key.equals(ContentLength.KEY)) {
			header = new ContentLength(value);
		} else {
			header = new Header(key, value);
		}
		return header;
	}
	
	public String decodeBody(IBuffer buffer, final int contentLength) {
		if(buffer.readable() < contentLength) {
			return null;
		}
		final byte[] bytes = new byte[contentLength];
		buffer.read(bytes);
		return new String(bytes);
	}

}
