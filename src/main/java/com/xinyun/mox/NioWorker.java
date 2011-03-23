package com.xinyun.mox;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NioWorker implements Runnable {
	
	private static final Log logger = LogFactory.getLog(NioWorker.class);
	
	private static final int SELECT_INTERVAL = 500;
	private static final int CLEANUP_INTERVAL = 256; 
	
	private int port;
	/** 0 use default */
	private int backlog;
	/** null use default */
	private String host;
	/** 0 use default */
	private int receiveBufferSize = 0;
	private boolean reuseAddress = true;
	
	private ServerSocketChannel listenChannel;
	private final Selector selector;
	private final AtomicBoolean wakeUp = new AtomicBoolean(false);
	private final SocketBufferPool bufferPool = new SocketBufferPool(true);
	private final CycleBufferPool[] cycleBufferPools = {new CycleBufferPool(false), new CycleBufferPool(true)};
	
	private final AtomicInteger writeTicket = new AtomicInteger();
	private final ConcurrentHashMap<NioSession, Integer> toWrite = new ConcurrentHashMap<NioSession, Integer>();
	private final ConcurrentLinkedQueue<NioSession> toCancelList = new ConcurrentLinkedQueue<NioSession>();
	private final HashMap<NioSession, Boolean> registeredWrite = new HashMap<NioSession, Boolean>();
	private final AtomicInteger toCanceled = new AtomicInteger();
	
	private final INioService nioService;
	
	public NioWorker(final INioService nioService, final ServerSocketChannel listenChannel) throws IOException {
		this.selector = Selector.open();
		this.nioService = nioService;
		this.listenChannel = listenChannel;
	}
	
	public NioWorker(final INioService nioService) throws IOException {
		this(nioService, null);
	}
	
	private void config(ServerSocketChannel serverSocketChannel) throws IOException {
		serverSocketChannel.configureBlocking(false);
		final ServerSocket serverSocket = serverSocketChannel.socket();
		
		SocketAddress endpoint = new InetSocketAddress(host, port);
		if(receiveBufferSize != 0) {
			serverSocket.setReceiveBufferSize(receiveBufferSize);
		}
		serverSocket.setReuseAddress(reuseAddress);
		if(backlog != 0) {
			serverSocket.bind(endpoint, backlog);
		} else {
			serverSocket.bind(endpoint);
		}
		serverSocket.setReuseAddress(reuseAddress);
	}
	
	public void run() {
		try {
			if(listenChannel == null) {
				listenChannel = ServerSocketChannel.open();
				config(listenChannel);
			}

			listenChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (Exception e) {
			logger.error("com.xinyun.mox.NioWorker.run()", e);
			throw new RuntimeException(e);
		}

		for(;;) {
			try {
				loop();
			} catch (ClosedSelectorException e) {
				logger.error("com.xinyun.mox.NioWorker.run()", e);
				throw e;
			}
		}
	}
	
	public void loop() {
		wakeUp.set(false);
		
		try {
			selector.select(SELECT_INTERVAL);
		} catch(IllegalArgumentException e) {
			logger.error("com.xinyun.mox.NioWorker.loop(): SELECT_INTERVAL: "+SELECT_INTERVAL, e);
			throw e;
		} catch (IOException e) {
			logger.error("loop select exception", e);
			return;
		}
		
		if(wakeUp.get()) {
			selector.wakeup();
		}

		process(selector.selectedKeys());
		processCancelList();
		processCanceled();
		processWrite();
	}
	
	private void processWrite() {
		for(final Map.Entry<NioSession, Integer> entry : toWrite.entrySet()) {
			final NioSession nioSession = entry.getKey();
			toWrite.remove(nioSession, entry.getValue());
			
			if(registeredWrite.put(nioSession, Boolean.TRUE) == null) {
				try {
					final SelectionKey selectionKey = nioSession.selectableChannel.keyFor(selector);
					if(selectionKey == null) {
						nioSession.selectableChannel.register(selector, SelectionKey.OP_WRITE, nioSession);
					} else {
						selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
					}
				} catch (ClosedChannelException e) {
					logger.error("register write exception", e);
					try {
						close(nioSession);
						return;
					} catch (IOException e1) {
						logger.error("close exception", e1);
						return;
					}
				}
			}
		}
	}
	
	private void processCanceled() {
		if(toCanceled.get() > CLEANUP_INTERVAL) {
			try {
				selector.selectNow();
			} catch (IOException e) {
				logger.error("processCancel", e);
				return;
			}
			toCanceled.set(0);
		}
	}
	
	private void processCancelList() {
		final ArrayList<NioSession> putBack = new ArrayList<NioSession>();
		for(NioSession nioSession = toCancelList.poll(); nioSession != null; nioSession = toCancelList.poll()) {
			if(toWrite.containsKey(nioSession)) {
				putBack.add(nioSession);
				continue;
			} else {
				final CycleBuffer writeBuffer = nioSession.writeBuffer;
				if(writeBuffer.readable() > 0) {
					putBack.add(nioSession);
					continue;
				}
			}
			try {
				close(nioSession);
			} catch (IOException e) {
				logger.error("process cancel fail", e);
			}
		}
		
		for(final NioSession nioSession : putBack) {
			toCancelList.add(nioSession);
		}
	}
	
	private void process(Set<SelectionKey> selectedKeys) {
		for(SelectionKey selectionKey : selectedKeys) {
			selectedKeys.remove(selectionKey);

			try {
				if (selectionKey.readyOps() == 0) {
					read(selectionKey);
				} else if (selectionKey.isAcceptable()) {
					ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
					SocketChannel socketChannel = null;
					try {
						socketChannel = serverSocketChannel.accept();
					} catch (IOException e) {
						logger.error("accept", e);
						continue;
					}
					if (socketChannel == null) {
						throw new AssertionError("serverSocketChannel.accept: " + socketChannel);
					}
					final NioSession session = NioSessionFactory.create(nioService, cycleBufferPools, this, socketChannel);
					fire(EventType.CREATE, session, null);
					fire(EventType.ACCEPT, session, null);
					try {
						socketChannel.configureBlocking(false);
						socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, session);
					} catch (ClosedChannelException e) {
						logger.error("channel close", e);
						throw new RuntimeException(e);
					} catch(IOException e) {
						logger.error("accept exception", e);
						continue;
					}
				} else {
					boolean other = true;
					if (selectionKey.isReadable()) {
						other = false;
						read(selectionKey);
					}
					if (selectionKey.isWritable()) {
						other = false;
						write(selectionKey);
					}
					if (other) {
						throw new AssertionError("selectKey: (readyOps:" + selectionKey.readyOps() + ", interestOps:" + selectionKey.interestOps()
								+ "), attachment:" + selectionKey.attachment());
					}
				}
			} catch (CancelledKeyException e) {
				final NioSession session = (NioSession) selectionKey.attachment();
				try {
					if(session != null) {
						close(session);
					}
				} catch(IOException e1) {
					logger.error("close excpetion", e1);
				}
			}
		}		
	}
	
	private void decode(final NioSession session) {
		final CycleBuffer buffer = session.readBuffer;
		buffer.storeR();
		try {
			final Object msg = session.decode();
			if(msg == null) {
				buffer.loadR();
			} else {
				fire(EventType.READ, session, msg);
			}
		} catch (DataNotEnough e) {
			buffer.loadR();
		} catch(Throwable e) {
			logger.error("decode exception", e);
			try {
				close(session);
				return;
			} catch (IOException e1) {
				logger.error("close exception", e1);
				return;
			}
		} finally {
			buffer.endR();
		}		
	}
	
	/**
	 * @param selectionKey
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	private void read(SelectionKey selectionKey) {
		final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		final NioSession session = (NioSession) selectionKey.attachment();
		
		final CycleBuffer buffer = session.readBuffer;
		
		for(;;) {
			final int writeable = buffer.writeable();
			if(writeable == 0) {
				//也许动态扩大readBuffer是一种解决方案，不过是否有这种必要？
				throw new BufferOverflowException();
			}
			final ByteBuffer byteBuffer = bufferPool.acquire(writeable);
			try {
				try {
					if (socketChannel.read(byteBuffer) < 0) {
						decode(session);
						close(session);
						fire(EventType.CLOSED_BY_PEER, session, null);
						return;
					}
				} catch(IOException e) {
					logger.error("read socket", e);
					try {
						close(session);
						return;
					} catch (IOException e1) {
						logger.error("close socket exception", e1);
						return;
					}
				}

				byteBuffer.flip();
				if(byteBuffer.remaining() == 0) {
					return;
				}
				
				for (;;) {
					// 当byteBuffer中的数据写入buffer时，可能会写不下，这时候数据会丢失，所以byteBuffer不能太大
					if(buffer.write(byteBuffer) == 0) {
						throw new BufferOverflowException();
					}
					decode(session);
					if (byteBuffer.remaining() == 0) {
						break;
					}
				}
			} finally {
				bufferPool.release(byteBuffer);
			}
		}
	}
	
	private void encode(final NioSession session) {
		final CycleBuffer buffer = session.writeBuffer;
		for(;;) {
			final Object msg = session.sendQueue.peek();
			if(msg == null) {
				removeWrite(session);
				break;
			}
			
			buffer.storeW();
			try {
				if(session.encode(msg) == false) {
					buffer.loadW();
				} else {
					session.sendQueue.remove();
				}
			} catch (SpaceNotEnough e) {
				buffer.loadW();
				break;
			} catch (Throwable e) {
				logger.error("encode exception", e);
				removeWrite(session);
				try {
					close(session);
					return;
				} catch (IOException e1) {
					logger.error("close exception", e1);
					return;
				}
			} finally {
				buffer.endW();
			}
		}		
	}
	
	/**
	 * @param selectionKey
	 * @throws IOException
	 */
	private void write(SelectionKey selectionKey) {
		final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		final NioSession session = (NioSession) selectionKey.attachment();
		final CycleBuffer buffer = session.writeBuffer;

		for(;;) {
			encode(session);
			
			final int readable = buffer.readable();
			if(readable == 0) {
				return;
			}

			for(;;) {
				final ByteBuffer byteBuffer = bufferPool.acquire(readable);
				try {
					buffer.read(byteBuffer);

					byteBuffer.flip();
					final int remaining = byteBuffer.remaining();
					int writted;
					try {
						writted = socketChannel.write(byteBuffer);
					} catch (IOException e) {
						logger.error("write exception", e);
						try {
							close(session);
							return;
						} catch (IOException e1) {
							logger.error("socket close exception", e1);
							return;
						}
					}
					final int moveBack = remaining - writted;
					if (moveBack > 0) {
						buffer.moveBackR(moveBack);
						return;
					}
					if(byteBuffer.remaining() == 0) {
						break;
					}
				} finally {
					bufferPool.release(byteBuffer);
				}
			}
		}
	}
	
	public void write(final NioSession nioSession) {
		toWrite.put(nioSession, writeTicket.incrementAndGet());
		if(registeredWrite.get(nioSession) == null) {
			if(wakeUp.compareAndSet(false, true)) {
				selector.wakeup();
			}
		}
	}
	
	public void cancel(final NioSession nioSession) {
		toCancelList.add(nioSession);
		if(wakeUp.compareAndSet(false, true)) {
			selector.wakeup();
		}
	}
	
	private void removeWrite(final NioSession session) {
		registeredWrite.remove(session);
		SelectionKey key = session.selectableChannel.keyFor(selector);
		if(key == null) {
			return;
		}
		
        if (!key.isValid()) {
            //TODO close
            return;
        }
        
        key.interestOps(key.interestOps() & ~(SelectionKey.OP_WRITE));
	}
	
	private void fire(final EventType type, final NioSession session, final Object message) {
		final Executor executor = nioService.getExecutor();
		if(executor == null) {
			try {
				fired(type, session, message);
			} catch(Throwable e) {
				logger.error("fire event "+type+", session: "+session.getSession()+", message: "+message, e);
			}
		} else {
			executor.execute(new Runnable(){
				@Override
				public void run() {
					fired(type, session, message);
				}});
		}
	}
	
	private void fired(final EventType type, final NioSession session, Object message) {
		if(type == EventType.ACCEPT) {
			session.accepted();
		} else if(type == EventType.CLOSED) {
			session.closed();
		} else if(type == EventType.CLOSED_BY_PEER) {
			session.closedByPeer();
		} else if(type == EventType.CREATE) {
			session.create();
		} else if(type == EventType.READ) {
			session.receivedMsg(message);
		}
	}
	
	public void close(final NioSession session) throws IOException {
		toCanceled.incrementAndGet();
		session.selectableChannel.close();
		fire(EventType.CLOSED, session, null);
	}

	public void setReceiveBufferSize(int receiveBufferSize) {
		this.receiveBufferSize = receiveBufferSize;
	}

	public int getReceiveBufferSize() {
		return receiveBufferSize;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
	}

	public int getBacklog() {
		return backlog;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

}
