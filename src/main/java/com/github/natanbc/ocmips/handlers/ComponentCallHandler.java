package com.github.natanbc.ocmips.handlers;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.memory.MemoryOperationException;
import com.github.natanbc.mipscpu.memory.MemoryHandler;
import com.github.natanbc.ocmips.RetryInNextTick;
import com.github.natanbc.ocmips.utils.ConversionHelpers;
import com.github.natanbc.ocmips.utils.MemoryUtils;
import li.cil.oc.api.machine.LimitReachedException;
import li.cil.oc.api.machine.Machine;
import net.minecraft.nbt.NBTTagCompound;

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
                    case ConversionHelpers.TYPE_INTEGER: args[argIdx++] = memory[i + 1]; break;
                    case ConversionHelpers.TYPE_FLOAT: args[argIdx++] = Float.intBitsToFloat(memory[i + 1]); break;
                    case ConversionHelpers.TYPE_STRING: args[argIdx++] = MemoryUtils.readString(cpu, memory[i+1]); break;
                    case ConversionHelpers.TYPE_ADDRESS: args[argIdx++] = MemoryUtils.readAddress(cpu, memory[i+1]); break;
                    case ConversionHelpers.TYPE_NULL: args[argIdx++] = null; break;
                    case ConversionHelpers.TYPE_SHORT: args[argIdx++] = (short)memory[i + 1]; break;
                    case ConversionHelpers.TYPE_BOOLEAN: args[argIdx++] = memory[i + 1] != 0; break;
                    default: throw new MemoryOperationException(address, MemoryOperationException.Reason.INVALID_VALUE);
                }
            }
            Object[] ret;
            try {
                ret = machine.invoke(componentAddress, method, args);
            } catch (LimitReachedException e) {
                throw new RetryInNextTick(e);
            } catch(Exception e) {
                e.printStackTrace();
                throw new MemoryOperationException(address, MemoryOperationException.Reason.ACCESS_ERROR);
            }
            memory[IDX_RETCOUNT] = ConversionHelpers.writeObjectsToMemory(cpu, ret,
                    memory[IDX_RETBUF], memory[IDX_RETBUFSIZE]);
            return;
        }
        if(address == base + IDX_ARGC*4) {
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
}
