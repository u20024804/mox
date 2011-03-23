package com.xinyun.mox;

import java.nio.ByteBuffer;

public class CycleBufferPerfmenceTest {
	
	public static final int DATA_SIZE = 102400;
	public static final int BUFFER_SIZE = 102400;
	public static final int TEST_TIMES = 1000000;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		test1();	//test1: 35904
		test2();	//test2: 25710
		test3();	//test3: 30079
	}
	
	public static void test1() {
		System.gc();
		byte[] src = new byte[DATA_SIZE];
		byte[] dst = new byte[DATA_SIZE];
		for(int i = 0; i < src.length; i++) {
			src[i] = (byte) (i & 0xff);
		}
		
		CycleBuffer buffer = new CycleBuffer(BUFFER_SIZE);
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < TEST_TIMES; ++i) {
			buffer.write(src);
			buffer.read(dst);
		}
		long end = System.currentTimeMillis();
		System.out.println("test1: "+(end - start));
	}
	
	public static void test2() {
		System.gc();
		byte[] src = new byte[DATA_SIZE];
		byte[] dst = new byte[DATA_SIZE];
		for(int i = 0; i < src.length; i++) {
			src[i] = (byte) (i & 0xff);
		}
		
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < TEST_TIMES; ++i) {
			buffer.rewind();
			buffer.put(src);
			buffer.flip();
			buffer.get(dst);
		}
		long end = System.currentTimeMillis();
		System.out.println("test2: "+(end - start));
	}
	
	public static void test3() {
		System.gc();
		byte[] src = new byte[DATA_SIZE];
		byte[] dst = new byte[DATA_SIZE];
		for(int i = 0; i < src.length; i++) {
			src[i] = (byte) (i & 0xff);
		}
		
		ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < TEST_TIMES; ++i) {
			buffer.rewind();
			buffer.put(src);
			buffer.flip();
			buffer.get(dst);
		}
		long end = System.currentTimeMillis();
		System.out.println("test3: "+(end - start));
	}


}
