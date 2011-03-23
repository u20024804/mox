package com.xinyun.mox;

import java.nio.channels.SelectableChannel;

public class NioSessionFactory {

	public static NioSession create(final INioService nioService, final CycleBufferPool[] cycleBufferPools,
									final NioWorker core, final SelectableChannel selectableChannel) {
		final ISession session = nioService.getSessionFactory().create();
		final IDecoder decoder = nioService.getDecoderFactory().create();
		final IEncoder endoder = nioService.getEncoderFactory().create();
		final NioSession nioSession = new NioSession(session, decoder, endoder, cycleBufferPools, core, selectableChannel);
		if(session instanceof AbstractSession) {
			final AbstractSession abstraceSession = (AbstractSession) session;
			abstraceSession.nioSession = nioSession;
		}
		return nioSession;
	}
	
}
