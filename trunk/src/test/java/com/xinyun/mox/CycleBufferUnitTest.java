package com.xinyun.mox;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CycleBufferUnitTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void test() {
		CycleBuffer buffer = new CycleBuffer();
		byte[] src = {1, 2, 3, 4};
		assertEquals(4, buffer.write(src));
		byte[] dst = new byte[4];
		assertEquals(4, buffer.read(dst));
		System.out.println(Arrays.toString(dst));
		assertEquals("[1, 2, 3, 4]", Arrays.toString(dst));
		
		buffer.write4Bytes(2345);
		int i = buffer.read4Bytes();
		System.out.println(i);
		assertEquals(2345, i);
		
		byte[] _1015 = new byte[1015];
		for(int b = 0; b < _1015.length; b++) {
			_1015[b] = (byte) (b & 0xff);
		}
		assertEquals(_1015.length, buffer.write(_1015));
		
		byte[] _1015_ = new byte[_1015.length];
		assertEquals(_1015.length, buffer.read(_1015_));
		for(int b = 0; b < _1015_.length; b++) {
			if(_1015_[b] != _1015[b]) {
				System.err.println("b: "+b+", _1015_[b]: "+_1015_[b]+", _1015[b]: "+_1015[b]);
				fail("see above");
			}
		}
		
		buffer.write4Bytes(4567);
		buffer.write4Bytes(7890);
		i = buffer.read4Bytes();
		System.out.println(i);
		assertEquals(4567, i);
		i = buffer.read4Bytes();
		System.out.println(i);
		assertEquals(7890, i);
		
		byte[] _1024 = new byte[1024];
		int writted = buffer.write(_1024);
		System.out.println(writted);
		System.out.println(buffer.capacity());
		System.out.println(buffer.writeable());
		System.out.println(buffer.readable());
		assertEquals(buffer.capacity(), writted);
		assertEquals(buffer.capacity(), buffer.readable());
		assertEquals(0, buffer.writeable());
	}
	
	@Test
	public void test2() {
		CycleBuffer buffer = new CycleBuffer(1024);
		for(int i = 0; i < 192; i++) {
			buffer.write4Bytes(i);
		}
		for(int i = 0; i < 192; i++) {
			assertEquals(i, buffer.read4Bytes());
		}

		
		for(int i = 0; i < 68; i++) {
			buffer.write1Byte((byte)(0xff & i));
			buffer.write2Bytes((short)(0xffff & i));
			buffer.write4Bytes(i);
			buffer.write8Bytes(i);
		}
		
		for(int i = 0; i < 68; i++) {
			assertEquals(0xff & i, buffer.read1Byte());
			assertEquals((short)(0xffff & i), buffer.read2Bytes());
			assertEquals(i, buffer.read4Bytes());
			assertEquals(i, buffer.read8Bytes());
		}
	}

}
