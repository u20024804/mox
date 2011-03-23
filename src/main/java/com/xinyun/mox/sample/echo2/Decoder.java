package com.xinyun.mox.sample.echo2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xinyun.mox.IBuffer;
import com.xinyun.mox.IDecoder;

public class Decoder implements IDecoder {
	
	private static final Log logger = LogFactory.getLog(Decoder.class);

	@Override
	public Byte decode(IBuffer buffer) {
		Byte b = buffer.read1Byte();
		logger.info("decoded: "+b);
		return b;
	}

}
