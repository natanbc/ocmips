package com.github.natanbc.ocmips.utils;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.memory.MemoryOperationException;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ConversionHelpers {
    public static final int
            TYPE_INTEGER = 1,
            TYPE_FLOAT = 2,
            TYPE_STRING = 3,
            TYPE_ADDRESS = 4,
            TYPE_NULL = 5,
            TYPE_SHORT = 6,
            TYPE_BOOLEAN = 7,
            TYPE_BYTE_ARRAY = 8;

    // struct retval { word type, word value, word next }
    public static int writeObjectsToMemory(MipsCPU cpu, Object[] arr, int addr, int size) throws MemoryOperationException {
        int valuesWritten = 0;
        int end = addr + size;
        int prev = 0;
        if(arr != null) {
            for(Object v : arr) {
                if(addr + 12 > end) break;
                int type = typeOf(v);
                IntBuffer buffer = toBuffer(v);
                if(buffer != null && addr + 12 + buffer.capacity() * 4 > end) {
                    break;
                }
                if(prev != 0) {
                    cpu.writeWord(prev + 8, addr);
                }
                prev = addr;
                cpu.writeWord(prev, type);
                cpu.writeWord(prev + 8, 0);
                addr += 12; //size of this retval
                if(isWordSize(type)) {
                    cpu.writeWord(prev + 4, buffer == null ? 0 : buffer.get(0));
                } else {
                    if(buffer == null) throw new AssertionError();
                    cpu.writeWord(prev + 4, prev + 12);
                    for(int i = 0; i < buffer.capacity(); i++) {
                        cpu.writeWord(prev + 12 + i*4, buffer.get(i));
                    }
                    //size of the blob after the retval
                    addr += buffer.capacity() * 4;
                }
                valuesWritten++;
            }
        }
        return valuesWritten;
    }

    public static IntBuffer toBuffer(Object value) {
        if(value == null) {
            return null;
        }
        if(value instanceof Boolean) {
            return MemoryUtils.allocateWords(1).put(((Boolean)value) ? 1 : 0);
        }
        if(value instanceof Integer || value instanceof Long) {
            return MemoryUtils.allocateWords(1).put(((Number)value).intValue());
        }
        if(value instanceof Float || value instanceof Double) {
            return MemoryUtils.allocateWords(1).put(Float.floatToRawIntBits(((Number)value).floatValue()));
        }
        if(value instanceof Short) {
            return MemoryUtils.allocateWords(1).put((Short)value);
        }
        if(value instanceof UUID) {
            UUID id = (UUID)value;
            ByteBuffer bb = MemoryUtils.allocateExact(16);
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
                int len = utf8.length + 1;
                return ((ByteBuffer)MemoryUtils.allocateRounding(len)
                        .put(utf8)
                        .put((byte)0)
                        .position(0))
                        .asIntBuffer();
            }
        }
        if(value instanceof byte[]) {
            byte[] array = (byte[])value;
            ByteBuffer buffer = MemoryUtils.allocateRounding(array.length + 4);
            buffer.position(4);
            buffer.put(array);
            buffer.position(0);
            return buffer.asIntBuffer()
                    .put(0, array.length);
        }
        return null;
    }

    public static int typeOf(Object o) {
        if(o == null) {
            return TYPE_NULL;
        }
        if(o instanceof Boolean) {
            return TYPE_BOOLEAN;
        }
        if(o instanceof Integer || o instanceof Long) {
            return TYPE_INTEGER;
        }
        if(o instanceof Float || o instanceof Double) {
            return TYPE_FLOAT;
        }
        if(o instanceof Short) {
            return TYPE_SHORT;
        }
        if(o instanceof UUID) {
            return TYPE_ADDRESS;
        }
        if(o instanceof String) {
            try {
                //noinspection ResultOfMethodCallIgnored
                UUID.fromString((String)o);
                return TYPE_ADDRESS;
            } catch(Exception e) {
                return TYPE_STRING;
            }
        }
        if(o instanceof byte[]) {
            return TYPE_BYTE_ARRAY;
        }
        System.out.printf("[ocmips] Unsupported return type %s for ComponentCallHandler\n", o.getClass());
        return TYPE_NULL;
    }

    private static boolean isWordSize(int type) {
        return type == TYPE_INTEGER || type == TYPE_FLOAT || type == TYPE_NULL ||
                type == TYPE_SHORT || type == TYPE_BOOLEAN;
    }
}
