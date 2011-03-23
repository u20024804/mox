package com.xinyun.mox.sample.echo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xinyun.mox.IBuffer;
import com.xinyun.mox.IDecoder;

public class Decoder implements IDecoder {
	
	private static final Log logger = LogFactory.getLog(Decoder.class);

	@Override
	public String decode(IBuffer buffer) {
		byte[] bs = new byte[1024];
		int i = 0;
		byte b;
		for(b = buffer.read1Byte(); b != '\r' && b != '\n'; b = buffer.read1Byte()) {
			bs[i++] = b;
		}
		
		final String s = new String(bs, 0, i);
		logger.info("decoded: "+s);
		return s;
	}

}
