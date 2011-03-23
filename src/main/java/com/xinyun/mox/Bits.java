package com.xinyun.mox;


public class Bits {
	
    static short swap(short x) {
        return (short)((x << 8) |
                       ((char)x >>> 8));
    }

    static char swap(char x) {
        return (char)((x << 8) |
                      (x >>> 8));
    }

    static int swap(int x) {
        return ((x << 24) |
                ((x & 0x0000ff00) <<  8) |
                ((x & 0x00ff0000) >>> 8) |
                (x >>> 24));
    }

    static long swap(long x) {
        return (((long)swap((int)x) << 32) |
                ((long)swap((int)(x >>> 32)) & 0xffffffffL));
    }


    // -- get/put char --

    static char makeChar(byte b1, byte b0) {
        return (char)((b1 << 8) | (b0 & 0xff));
    }

    static byte char1(char x) { return (byte)(x >> 8); }
    static byte char0(char x) { return (byte)(x >> 0); }


    // -- get/put short --

    static short makeShort(byte b1, byte b0) {
        return (short)((b1 << 8) | (b0 & 0xff));
    }

    static byte short1(short x) { return (byte)(x >> 8); }
    static byte short0(short x) { return (byte)(x >> 0); }

    // -- get/put int --

    static int makeInt(byte b3, byte b2, byte b1, byte b0) {
        return (((b3 & 0xff) << 24) |
                ((b2 & 0xff) << 16) |
                ((b1 & 0xff) <<  8) |
                ((b0 & 0xff) <<  0));
    }

    static byte int3(int x) { return (byte)(x >> 24); }
    static byte int2(int x) { return (byte)(x >> 16); }
    static byte int1(int x) { return (byte)(x >>  8); }
    static byte int0(int x) { return (byte)(x >>  0); }


    // -- get/put long --

    static long makeLong(byte b7, byte b6, byte b5, byte b4,
                                 byte b3, byte b2, byte b1, byte b0)
    {
        return ((((long)b7 & 0xff) << 56) |
                (((long)b6 & 0xff) << 48) |
                (((long)b5 & 0xff) << 40) |
                (((long)b4 & 0xff) << 32) |
                (((long)b3 & 0xff) << 24) |
                (((long)b2 & 0xff) << 16) |
                (((long)b1 & 0xff) <<  8) |
                (((long)b0 & 0xff) <<  0));
    }

    static byte long7(long x) { return (byte)(x >> 56); }
    static byte long6(long x) { return (byte)(x >> 48); }
    static byte long5(long x) { return (byte)(x >> 40); }
    static byte long4(long x) { return (byte)(x >> 32); }
    static byte long3(long x) { return (byte)(x >> 24); }
    static byte long2(long x) { return (byte)(x >> 16); }
    static byte long1(long x) { return (byte)(x >>  8); }
    static byte long0(long x) { return (byte)(x >>  0); }
    

}
