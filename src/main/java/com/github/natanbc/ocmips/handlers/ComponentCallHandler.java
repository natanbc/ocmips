package com.github.natanbc.ocmips.handlers;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.memory.MemoryOperationException;
import com.github.natanbc.mipscpu.memory.MemoryHandler;
import com.github.natanbc.ocmips.utils.MemoryUtils;
import li.cil.oc.api.machine.Machine;
import net.minecraft.nbt.NBTTagCompound;

import java.nio.IntBuffer;
import java.util.UUID;

//struct arg { word type, word value }
//struct retval { word type, word value, word next }
//struct component_map {
// word call;
// word retBuf;
// word retBufSize;
// word retCount;
// word argc;
// struct arg args[argc]
//}
public class ComponentCallHandler implements MemoryHandler {
    public static final int
            TYPE_INTEGER = 1,
            TYPE_FLOAT = 2,
            TYPE_STRING = 3,
            TYPE_ADDRESS = 4,
            TYPE_NULL = 5,
            TYPE_SHORT = 6,
            TYPE_BYTE_ARRAY = 7;
    
    private static final int IDX_RETBUF = 0, IDX_RETBUFSIZE = 1,
            IDX_RETCOUNT = 2, IDX_ARGC = 3, IDX_ARGS = 4;
    
    private final Machine machine;
    private final String componentAddress;
    private final String method;
    private final int maxArg;
    private final int[] memory;
    private int base;
    
    public ComponentCallHandler(Machine machine, String componentAddress, String method, int maxArg) {
        this.machine = machine;
        this.componentAddress = componentAddress;
        this.method = method;
        this.maxArg = maxArg;
        this.memory = new int[maxArg * 2 + IDX_ARGS];
    }

    public String getComponentAddress() {
        return componentAddress;
    }

    public String getMethod() {
        return method;
    }

    public int getMaxArg() {
        return maxArg;
    }

    public void save(NBTTagCompound tag) {
        tag.setIntArray("memory", memory);
    }

    public void restore(NBTTagCompound tag) {
        int[] m = tag.getIntArray("memory");
        System.arraycopy(m, 0, memory, 0, m.length);
    }

    @Override
    public void onAttach(MipsCPU cpu, int baseAddress) {
        this.base = baseAddress;
    }
    
    @Override
    public int read(MipsCPU cpu, int address) {
        if(address == base) {
            return 0;
        }
        return memory[(address - base - 4) / 4];
    }
    
    @Override
    public void write(MipsCPU cpu, int address, int value) throws MemoryOperationException {
        if(address == base) {
            Object[] args = new Object[memory[IDX_ARGC]];
            int argIdx = 0;
            for(int i = IDX_ARGS; i < memory.length; i += 2) {
                switch(memory[i]) {
                    case TYPE_INTEGER: args[argIdx++] = memory[i + 1]; break;
                    case TYPE_FLOAT: args[argIdx++] = Float.intBitsToFloat(memory[i + 1]); break;
                    case TYPE_STRING: args[argIdx++] = MemoryUtils.readString(cpu, memory[i+1]); break;
                    case TYPE_ADDRESS: args[argIdx++] = MemoryUtils.readAddress(cpu, memory[i+1]); break;
                    case TYPE_NULL: args[argIdx++] = null; break;
                    case TYPE_SHORT: args[argIdx++] = (short)memory[i + 1]; break;
                    default: throw new MemoryOperationException(address, MemoryOperationException.Reason.INVALID_VALUE);
                }
            }
            Object[] ret;
            try {
                ret = machine.invoke(componentAddress, method, args);
            } catch(Exception e) {
                e.printStackTrace();
                throw new MemoryOperationException(address, MemoryOperationException.Reason.ACCESS_ERROR);
            }
            int valuesWritten = 0;
            int remaining = memory[IDX_RETBUFSIZE];
            int addr = memory[IDX_RETBUF];
            int prev = 0;
            if(ret != null) {
                for(Object v : ret) {
                    if(remaining < 12) break;
                    int type = typeOf(v);
                    IntBuffer buffer = MemoryUtils.toBuffer(ret);
                    if(buffer != null && buffer.capacity() > remaining) {
                        break;
                    }
                    if(prev != 0) {
                        cpu.writeWord(prev + 8, addr);
                    }
                    prev = addr;
                    cpu.writeWord(base, type);
                    cpu.writeWord(base + 8, 0);
                    if(type == TYPE_INTEGER || type == TYPE_FLOAT || type == TYPE_NULL) {
                        cpu.writeWord(base + 4, buffer == null ? 0 : buffer.get(0));
                    } else {
                        if(buffer == null) throw new AssertionError();
                        cpu.writeWord(base + 4, base + 12);
                        for(int i = 0; i < buffer.capacity(); i++) {
                            cpu.writeWord(base + 12 + i*4, buffer.get(i));
                        }
                    }
                    valuesWritten++;
                }
            }
            memory[IDX_RETCOUNT] = valuesWritten;
            return;
        }
        if(address == base + 4) {
            if(value < 0 || value > maxArg) {
                throw new MemoryOperationException(address, MemoryOperationException.Reason.INVALID_VALUE);
            }
        }
        memory[(address - base - 4) / 4] = value;
    }
    
    @Override
    public int memorySize() {
        return 4            // call
             + 4            // retBuf
             + 4            // retBufSize
             + 4            // argc
             + 8 * maxArg;  // args
    }
    
    private static int typeOf(Object o) {
        if(o == null) {
            return TYPE_NULL;
        }
        if(o instanceof Integer) {
            return TYPE_INTEGER;
        }
        if(o instanceof Float) {
            return TYPE_FLOAT;
        }
        if(o instanceof Short) {
            return TYPE_SHORT;
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
}
