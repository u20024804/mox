package com.xinyun.mox.sample.echo2;

import com.xinyun.mox.IDecoder;
import com.xinyun.mox.IDecoderFactory;

public class DecoderFactory implements IDecoderFactory {

	@Override
	public IDecoder create() {
		return new Decoder();
	}

}
