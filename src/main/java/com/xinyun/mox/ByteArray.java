package com.xinyun.mox;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ByteArray {
	
	private final ArrayList<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
	
	private int index = 0;
	private int marked = -1;
	
	public void mark() {
		for(int i = index; i < buffers.size(); i++) {
			buffers.get(i).mark();
		}
		marked = index;
	}
	
	public void reset() {
		if(marked < 0) {
			throw new AssertionError("no marked but reset");
		}
		for(int i = marked; i < index; i++) {
			buffers.get(i).reset();
		}
		index = marked;
		marked = -1;
	}
	
	public void flip() {
		
	}

}
