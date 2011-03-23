package com.xinyun.mox;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.concurrent.ArrayBlockingQueue;


public class NioSession {
	
	public static final int DEFAULT_BUFFER_SIZE = 64 * 1024;
	public static final int DEFAULT_SEND_QUEUE_SIZE = 256;

	final CycleBuffer readBuffer;
	final CycleBuffer writeBuffer;
	
	final SelectableChannel selectableChannel;
	
	final ArrayBlockingQueue<Object> sendQueue;
	
	private final NioWorker core;
	
	private final ISession session;
	private final IDecoder decoder;
	private final IEncoder encoder;
	
	private final CycleBufferPool cycleBufferPool;

	NioSession(final ISession session, final IDecoder decoder, final IEncoder encoder, final CycleBufferPool[] cycleBufferPools,
			final NioWorker core, final SelectableChannel selectableChannel, final boolean bigEndian, final int readBufferSize, final int writeBufferSize, final int sendQueueSize) {
		this.session = session;
		this.decoder = decoder;
		this.encoder = encoder;
		this.core = core;
		this.selectableChannel = selectableChannel;
		this.cycleBufferPool = cycleBufferPools[bigEndian ? 1 : 0];
		readBuffer = cycleBufferPool.acquire(readBufferSize);
		writeBuffer = cycleBufferPool.acquire(writeBufferSize);
		sendQueue = new ArrayBlockingQueue<Object>(sendQueueSize);
	}
	
	NioSession(final ISession session, final IDecoder decoder, final IEncoder encoder, final CycleBufferPool[] cycleBufferPools,
			final NioWorker core, final SelectableChannel selectableChannel, final int bufferSize) {
		this(session, decoder, encoder, cycleBufferPools, core, selectableChannel, true, bufferSize, bufferSize, DEFAULT_SEND_QUEUE_SIZE);
	}
	
	NioSession(final ISession session, final IDecoder decoder, final IEncoder encoder, final CycleBufferPool[] cycleBufferPools,
			final NioWorker core, final SelectableChannel selectableChannel) {
		this(session, decoder, encoder, cycleBufferPools, core, selectableChannel, true, DEFAULT_BUFFER_SIZE, DEFAULT_BUFFER_SIZE, DEFAULT_SEND_QUEUE_SIZE);
	}
	
	void receivedMsg(Object msg) {
		session.receivedMsg(msg);
	}
	
	void sendMsg(Object msg) {
		core.write(this);
		sendQueue.add(msg);
	}
	
	void close() throws IOException {
		core.cancel(this);
	}
	
	Object decode() {
		return decoder.decode(readBuffer);
	}
	
	boolean encode(Object message) {
		return encoder.encoder(writeBuffer, message);
	}
	
	void create() {
		session.create();
	}

	void accepted() {
		session.accepted();
	}

	void closed() {
		release();
		session.closed();
	}
	
	private void release() {
		cycleBufferPool.release(readBuffer);
		cycleBufferPool.release(writeBuffer);
	}
	
	void closedByPeer() {
		release();
		session.closedByPeer();
	}
	
	ISession getSession() {
		return session;
	}

}
