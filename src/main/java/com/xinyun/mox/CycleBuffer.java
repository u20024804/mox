package com.xinyun.mox;

import java.nio.ByteBuffer;

public class CycleBuffer implements IBuffer {
	
	private static final int SHADOW_SIZE = 8 - 1;
	private static final int MIN_CAPACITY = 1024 - 1 - SHADOW_SIZE;
	
	private final boolean bigEndian;
	
	private int readIndex = 0;
	private int readIndexStored = -1;
	private int writeIndex = 0;
	private int writeIndexStored = -1;
	
	private final byte[] array;
	private final int capacity;
	private final int length;
	
	private final byte[] byte1 = new byte[1];
	private final byte[] byte2 = new byte[2];
	private final byte[] byte4 = new byte[4];
	private final byte[] byte8 = new byte[8];
	
	public CycleBuffer(final boolean bigEndian, final int capacity) {
		this.bigEndian = bigEndian;
		if(capacity < MIN_CAPACITY) {
			this.capacity = MIN_CAPACITY;
		} else {
			this.capacity = capacity;
		}
		this.length = this.capacity + 1;
		this.array = new byte[this.length + SHADOW_SIZE];
	}
	
	public CycleBuffer(final int capacity) {
		this(true, capacity);
	}
	
	public CycleBuffer() {
		this(true, 0);
	}

	
	public int readable() {
		final int ret = writeIndex - readIndex;
		return ret >= 0 ? ret : ret + length;		
	}
	
	public int writeable() {
		return capacity - readable();
	}
	
	public int write(final byte[] src, final int offset, final int size) {
		if(writeable() < size) {
			throw new SpaceNotEnough();
		}
		
		final int firstPartLeft = writeIndex == 0 ? capacity : length - writeIndex;
		final int firstPartSize = firstPartLeft > size ? size : firstPartLeft;
		
		if(readIndex > writeIndex && writeIndex + firstPartSize >= readIndex) {
			final int writeSize = readIndex - writeIndex;
			System.arraycopy(src, offset, array, writeIndex, writeSize);
			writeIndex = readIndex - 1;
			return writeSize;
		}
		
		if(writeIndex + size > this.length) {
			if(writeIndex + size <= this.array.length) {
				System.arraycopy(src, offset, array, writeIndex, size);
			} else {
				System.arraycopy(src, offset, array, writeIndex, firstPartSize + SHADOW_SIZE);
			}
		} else {
			System.arraycopy(src, offset, array, writeIndex, firstPartSize);
		}
		if(writeIndex < SHADOW_SIZE) {
			System.arraycopy(array, 0, array, length, SHADOW_SIZE);
		}
		writeIndex += firstPartSize;
		
		final int leftSize = size - firstPartSize;
		if(leftSize == 0 || firstPartSize == capacity) {
			return firstPartSize;
		}
		
		final int secondPartLeft = readIndex - 1;
		if(secondPartLeft <= 0) {
			return firstPartSize;
		}
		final int secondPartSize = secondPartLeft > leftSize ? leftSize : secondPartLeft;
		if(secondPartSize >= readIndex) {
			final int writeSize = readIndex - 1;
			System.arraycopy(src, offset + firstPartSize, array, 0, writeSize);
			writeIndex = readIndex - 1;
			return firstPartSize + writeSize;
		}
		System.arraycopy(src, offset + firstPartSize, array, 0, secondPartSize);
		writeIndex = secondPartSize;
		
		return firstPartSize + secondPartSize;
	}
	
	int write(final ByteBuffer byteBuffer, final int size) {
		final int firstPartLeft = writeIndex == 0 ? capacity : length - writeIndex;
		final int firstPartSize = firstPartLeft > size ? size : firstPartLeft;
		
		if(readIndex > writeIndex && writeIndex + firstPartSize >= readIndex) {
			final int writeSize = readIndex - writeIndex;
			byteBuffer.get(array, writeIndex, writeSize);
			writeIndex = readIndex - 1;
			return writeSize;
		}
		
		if(writeIndex + size > this.length) {
			if(writeIndex + size <= this.array.length) {
				byteBuffer.get(array, writeIndex, size);
			} else {
				byteBuffer.get(array, writeIndex, firstPartSize + SHADOW_SIZE);
			}
		} else {
			byteBuffer.get(array, writeIndex, firstPartSize);
		}
		if(writeIndex < SHADOW_SIZE) {
			System.arraycopy(array, 0, array, length, SHADOW_SIZE);
		}
		writeIndex += firstPartSize;
		
		final int leftSize = size - firstPartSize;
		if(leftSize == 0 || firstPartSize == capacity) {
			return firstPartSize;
		}
		
		final int secondPartLeft = readIndex - 1;
		if(secondPartLeft <= 0) {
			return firstPartSize;
		}
		final int secondPartSize = secondPartLeft > leftSize ? leftSize : secondPartLeft;
		if(secondPartSize >= readIndex) {
			final int writeSize = readIndex - 1;
			byteBuffer.get(array, 0, writeSize);
			writeIndex = readIndex - 1;
			return firstPartSize + writeSize;
		}
		byteBuffer.get(array, 0, secondPartSize);
		writeIndex = secondPartSize;
		
		return firstPartSize + secondPartSize;
	}
	
	int write(final ByteBuffer byteBuffer) {
		return write(byteBuffer, byteBuffer.remaining());
	}

	public int write(final byte[] src) {
		return write(src, 0, src.length);
	}
	
	
	public int read(final byte[] dst, final int offset, final int size) {
		if(readable() < size) {
			throw new DataNotEnough();
		}
		
		if(readIndex == writeIndex) {
			return 0;
		}
		
		if(writeIndex > readIndex) {
			final int dataLeft = writeIndex - readIndex;
			final int readedSize = dataLeft > size ? size : dataLeft;
			System.arraycopy(array, readIndex, dst, offset, readedSize);
			readIndex += readedSize;
			return readedSize;
		}
		
		final int firstPartLeft = length - readIndex;
		final int firstPartSize = firstPartLeft > size ? size : firstPartLeft;
		System.arraycopy(array, readIndex, dst, offset, firstPartSize);
		readIndex += firstPartSize;
		if(readIndex == length) {
			readIndex = 0;
		}
		
		final int sizeLeft = size - firstPartSize;
		if(sizeLeft == 0) {
			return firstPartSize;
		}
		
		final int secondPartLeft = writeIndex;
		final int secondPartSize = secondPartLeft > sizeLeft ? sizeLeft : secondPartLeft;
		System.arraycopy(array, readIndex, dst, offset + firstPartSize, secondPartSize);
		readIndex += secondPartSize;
		
		return firstPartSize + secondPartSize;
	}
	
	int read(final ByteBuffer byteBuffer, final int size) {
		if(readIndex == writeIndex) {
			return 0;
		}
		
		if(writeIndex > readIndex) {
			final int dataLeft = writeIndex - readIndex;
			final int readedSize = dataLeft > size ? size : dataLeft;
			byteBuffer.put(array, readIndex, readedSize);
			readIndex += readedSize;
			return readedSize;
		}
		
		final int firstPartLeft = length - readIndex;
		final int firstPartSize = firstPartLeft > size ? size : firstPartLeft;
		byteBuffer.put(array, readIndex, firstPartSize);
		readIndex += firstPartSize;
		if(readIndex == length) {
			readIndex = 0;
		}
		
		final int sizeLeft = size - firstPartSize;
		if(sizeLeft == 0) {
			return firstPartSize;
		}
		
		final int secondPartLeft = writeIndex;
		final int secondPartSize = secondPartLeft > sizeLeft ? sizeLeft : secondPartLeft;
		byteBuffer.put(array, readIndex, secondPartSize);
		readIndex += secondPartSize;
		
		return firstPartSize + secondPartSize;
	}
	
	public int read(final byte[] dst) {
		return read(dst, 0, dst.length);
	}
	
	int read(final ByteBuffer byteBuffer) {
		return read(byteBuffer, byteBuffer.remaining());
	}
	
	
	public void write1Byte(byte value) {
		if(writeable() < byte1.length) {
			throw new SpaceNotEnough();
		}
		byte1[0] = value;
		if(write(byte1) != byte1.length) {
			throw new AssertionError();
		}
	}
	
	public void write2Bytes(short value) {
		if(writeable() < byte2.length) {
			throw new SpaceNotEnough();
		}
		if(bigEndian) {
			byte2[1] = Bits.short0(value);
			byte2[0] = Bits.short1(value);
		} else {
			byte2[0] = Bits.short0(value);
			byte2[1] = Bits.short1(value);
		}
		if(write(byte2) != byte2.length) {
			throw new AssertionError();
		}
	}
	
	public void write4Bytes(int value) {
		if(writeable() < byte4.length) {
			throw new SpaceNotEnough();
		}
		if(bigEndian) {
			byte4[3] = Bits.int0(value);
			byte4[2] = Bits.int1(value);
			byte4[1] = Bits.int2(value);
			byte4[0] = Bits.int3(value);
		} else {
			byte4[0] = Bits.int0(value);
			byte4[1] = Bits.int1(value);
			byte4[2] = Bits.int2(value);
			byte4[3] = Bits.int3(value);			
		}
		if(write(byte4) != byte4.length) {
			throw new AssertionError();
		}
	}
	
	public void write8Bytes(long value) {
		if(writeable() < byte8.length) {
			throw new SpaceNotEnough();
		}
		if(bigEndian) {
			byte8[7] = Bits.long0(value);
			byte8[6] = Bits.long1(value);
			byte8[5] = Bits.long2(value);
			byte8[4] = Bits.long3(value);
			byte8[3] = Bits.long4(value);
			byte8[2] = Bits.long5(value);
			byte8[1] = Bits.long6(value);
			byte8[0] = Bits.long7(value);
		} else {
			byte8[0] = Bits.long0(value);
			byte8[1] = Bits.long1(value);
			byte8[2] = Bits.long2(value);
			byte8[3] = Bits.long3(value);
			byte8[4] = Bits.long4(value);
			byte8[5] = Bits.long5(value);
			byte8[6] = Bits.long6(value);
			byte8[7] = Bits.long7(value);			
		}
		if(write(byte8) != byte8.length) {
			throw new AssertionError();
		}
	}
	
	public byte read1Byte() {
		if(readable() < byte1.length) {
			throw new DataNotEnough();
		}
		
		byte1[0] = array[readIndex++];
		readIndex %= length;
		
		return byte1[0];
	}
	
	public short read2Bytes() {
		if(readable() < byte2.length) {
			throw new DataNotEnough();
		}
		
		byte2[0] = array[readIndex++];
		byte2[1] = array[readIndex++];
		readIndex %= length;
		
		if(bigEndian) {
			return Bits.makeShort(byte2[0], byte2[1]);
		} else {
			return Bits.makeShort(byte2[1], byte2[0]);
		}
	}
	
	public int read4Bytes() {
		if(readable() < byte4.length) {
			throw new DataNotEnough();
		}
		
		byte4[0] = array[readIndex++];
		byte4[1] = array[readIndex++];
		byte4[2] = array[readIndex++];
		byte4[3] = array[readIndex++];
		readIndex %= length;
		
		if(bigEndian) {
			return Bits.makeInt(byte4[0], byte4[1], byte4[2], byte4[3]);
		} else {
			return Bits.makeInt(byte4[3], byte4[2], byte4[1], byte4[0]);
		}
	}
	
	public long read8Bytes() {
		if(readable() < byte8.length) {
			throw new DataNotEnough();
		}
		
		byte8[0] = array[readIndex++];
		byte8[1] = array[readIndex++];
		byte8[2] = array[readIndex++];
		byte8[3] = array[readIndex++];
		byte8[4] = array[readIndex++];
		byte8[5] = array[readIndex++];
		byte8[6] = array[readIndex++];
		byte8[7] = array[readIndex++];
		readIndex %= length;
		
		if(bigEndian) {
			return Bits.makeLong(byte8[0], byte8[1], byte8[2], byte8[3], byte8[4], byte8[5], byte8[6], byte8[7]);
		} else {
			return Bits.makeLong(byte8[7], byte8[6], byte8[5], byte8[4], byte8[3], byte8[2], byte8[1], byte8[0]);
		}
	}

	public int capacity() {
		return capacity;
	}
	
	void storeR() {
		readIndexStored = readIndex;
	}
	
	public void moveBackR(int step) {
		readIndex -= step;
		if(readIndex < 0) {
			readIndex += length;
		}
	}
	
	void loadR() {
		if(readIndexStored < 0) {
			throw new IllegalStateException("readIndexStored: "+readIndexStored);
		}
		readIndex = readIndexStored;
	}
	
	void endR() {
		readIndexStored = -1;
	}
	
	void storeW() {
		writeIndexStored = writeIndex;
	}
	
	void moveBackW(int step) {
		writeIndex -= step;
		if(writeIndex < 0) {
			writeIndex += length;
		}
	}
	
	void loadW() {
		if(writeIndexStored < 0) {
			throw new IllegalStateException("writeIndexStored: "+writeIndexStored);
		}
		writeIndex = writeIndexStored;
	}
	
	void endW() {
		writeIndexStored = -1;
	}
	
	void clear() {
		readIndex = 0;
		writeIndex = 0;
		readIndexStored = -1;
		writeIndexStored = -1;
	}

}
