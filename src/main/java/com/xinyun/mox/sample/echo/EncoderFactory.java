package com.xinyun.mox.sample.echo;

import com.xinyun.mox.IEncoder;
import com.xinyun.mox.IEncoderFactory;

public class EncoderFactory implements IEncoderFactory {

	@Override
	public IEncoder create() {
		return new Encoder();
	}

}
