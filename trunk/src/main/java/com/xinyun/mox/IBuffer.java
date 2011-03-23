package com.xinyun.mox;

public interface IBuffer {
	
	int readable();
	int writeable();
	
	int write(final byte[] src, final int offset, final int size);
	int write(final byte[] src);
	int read(final byte[] dst, final int offset, final int size);
	int read(final byte[] dst);
	
	void write1Byte(byte value);
	void write2Bytes(short value);
	void write4Bytes(int value);
	void write8Bytes(long value);
	byte read1Byte();
	short read2Bytes();
	int read4Bytes();
	long read8Bytes();
	int capacity();
	
	void moveBackR(int step);

}
