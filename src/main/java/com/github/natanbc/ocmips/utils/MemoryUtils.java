package com.github.natanbc.ocmips.utils;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.memory.MemoryOperationException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.UUID;

public class MemoryUtils {
    public static ByteBuffer allocateUnchecked(int size) {
        return ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN);
    }

    public static ByteBuffer allocateExact(int size) {
        if(!isExactWordCount(size)) {
            throw new IllegalArgumentException("Size is not a multiple of 4: " + size);
        }
        return allocateUnchecked(size);
    }

    public static IntBuffer allocateWords(int words) {
        return allocateUnchecked(words * 4).asIntBuffer();
    }

    public static ByteBuffer allocateRounding(int size) {
        //round size up to the next multiple of 4 (or size if it's already one)
        return allocateUnchecked((size + 3) & (-4));
    }

    public static ByteBuffer wrapExact(byte[] array) {
        if(!isExactWordCount(array.length)) {
            throw new IllegalArgumentException("Length is not a multiple of 4: " + array.length);
        }
        return ByteBuffer.wrap(array);
    }

    public static ByteBuffer wrap(byte[] array) {
        if(isExactWordCount(array.length)) {
            return wrapExact(array);
        }
        ByteBuffer b = allocateRounding(array.length);
        b.put(array).position(0);
        return b;
    }

    public static boolean isExactWordCount(int bytes) {
        return (bytes & 0b11) == 0;
    }

    public static String readString(MipsCPU cpu, int address) throws MemoryOperationException {
        if(address == 0) return null;
        StringBuilder sb = new StringBuilder();
        int b;
        while((b = cpu.readByte(address)) != 0) {
            sb.append((char)b);
            //no massive strings
            if(sb.length() > 2048) {
                throw new MemoryOperationException(address, MemoryOperationException.Reason.INVALID_VALUE);
            }
            address++;
        }
        return sb.toString();
    }
    
    public static String readAddress(MipsCPU cpu, int address) throws MemoryOperationException {
        if(address == 0) return null;
        ByteBuffer buffer = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        IntBuffer words = buffer.asIntBuffer();
        for(int i = 0; i < 4; i++) {
            words.put(cpu.readWord(address + i * 4));
        }
        LongBuffer lb = buffer.asLongBuffer();
        return new UUID(lb.get(0), lb.get(1)).toString();
    }

    public static byte[] readByteArray(MipsCPU cpu, int address) throws MemoryOperationException {
        if(address == 0) return null;
        int len = cpu.readWord(address);
        if(len > 2048) {
            throw new MemoryOperationException(address, MemoryOperationException.Reason.INVALID_VALUE);
        }
        byte[] dest = new byte[len];
        for(int i = 0; i < dest.length; i++) {
            dest[i] = (byte)cpu.readByte(address + 4 + i);
        }
        return dest;
    }
}
