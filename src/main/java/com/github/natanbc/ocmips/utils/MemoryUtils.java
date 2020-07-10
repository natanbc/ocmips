package com.github.natanbc.ocmips.utils;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.memory.MemoryOperationException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class MemoryUtils {
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
    
    public static IntBuffer toBuffer(Object value) {
        if(value == null) {
            return null;
        }
        if(value instanceof Integer) {
            return IntBuffer.allocate(1).put((Integer)value);
        }
        if(value instanceof Float) {
            return IntBuffer.allocate(1).put(Float.floatToRawIntBits((Float)value));
        }
        if(value instanceof Short) {
            return IntBuffer.allocate(1).put((Short)value);
        }
        if(value instanceof UUID) {
            UUID id = (UUID)value;
            ByteBuffer bb = ByteBuffer.allocate(16);
            bb.asLongBuffer()
                    .put(id.getMostSignificantBits())
                    .put(id.getLeastSignificantBits());
            return bb.asIntBuffer();
        }
        if(value instanceof String) {
            try {
                return toBuffer(UUID.fromString((String)value));
            } catch(Exception e) {
                byte[] utf8 = ((String)value).getBytes(StandardCharsets.UTF_8);
                return ByteBuffer.allocate(utf8.length + 1)
                               .put(utf8)
                               .put((byte)0)
                               .asIntBuffer();
            }
        }
        if(value instanceof byte[]) {
            byte[] b = (byte[])value;
            //if length not a multiple of 4, round it up to a multiple of 4.
            if(b.length % 4 != 0) {
                b = Arrays.copyOf(b, (b.length & ~0b11) + 4);
            }
            return ByteBuffer.wrap(b).asIntBuffer();
        }
        return null;
    }
}
