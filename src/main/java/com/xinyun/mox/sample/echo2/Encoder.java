package com.xinyun.mox.sample.echo2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xinyun.mox.IBuffer;
import com.xinyun.mox.IEncoder;

public class Encoder implements IEncoder {
	
	private static final Log logger = LogFactory.getLog(Encoder.class);

	@Override
	public boolean encoder(IBuffer buffer, Object message) {
		logger.info("encode: "+message);
		buffer.write1Byte(((Byte)message));
		return true;
	}

}
