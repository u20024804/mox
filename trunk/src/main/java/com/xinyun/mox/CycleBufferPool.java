/*
 * Copyright 2010 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.xinyun.mox;

import java.lang.ref.SoftReference;

/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @version $Rev$, $Date$
 */
final class CycleBufferPool {

    private static final int POOL_SIZE = 8;
    
    @SuppressWarnings("unchecked")
    private final SoftReference<CycleBuffer>[] pool = new SoftReference[POOL_SIZE];
    
    private final boolean bigEndian;

    CycleBufferPool(final boolean bigEndian) {
    	this.bigEndian = bigEndian;
    }

    /**
     * 这个方法是非线程安全的
     * @param size
     * @return
     */
    final CycleBuffer acquire(int size) {
        final SoftReference<CycleBuffer>[] pool = this.pool;
        for (int i = 0; i < POOL_SIZE; i ++) {
            SoftReference<CycleBuffer> ref = pool[i];
            if (ref == null) {
                continue;
            }

            CycleBuffer buf = ref.get();
            if (buf == null) {
                pool[i] = null;
                continue;
            }

            if (buf.capacity() < size) {
                continue;
            }

            pool[i] = null;

            buf.clear();
            return buf;
        }

        final CycleBuffer buf = new CycleBuffer(bigEndian, normalizeCapacity(size));
        buf.clear();
        return buf;
    }

    final void release(CycleBuffer buffer) {
        final SoftReference<CycleBuffer>[] pool = this.pool;
        for (int i = 0; i < POOL_SIZE; i ++) {
            SoftReference<CycleBuffer> ref = pool[i];
            if (ref == null || ref.get() == null) {
                pool[i] = new SoftReference<CycleBuffer>(buffer);
                return;
            }
        }

        // pool is full - replace one
        final int capacity = buffer.capacity();
        for (int i = 0; i< POOL_SIZE; i ++) {
            SoftReference<CycleBuffer> ref = pool[i];
            CycleBuffer pooled = ref.get();
            if (pooled == null) {
                pool[i] = null;
                continue;
            }

            if (pooled.capacity() < capacity) {
                pool[i] = new SoftReference<CycleBuffer>(buffer);
                return;
            }
        }
    }

    private static final int normalizeCapacity(int capacity) {
        // Normalize to multiple of 1024
        int q = capacity >>> 10;
        int r = capacity & 1023;
        if (r != 0) {
            q ++;
        }
        return q << 10;
    }
}