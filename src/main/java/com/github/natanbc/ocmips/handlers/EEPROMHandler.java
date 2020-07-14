package com.github.natanbc.ocmips.handlers;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.memory.MemoryHandler;
import com.github.natanbc.mipscpu.memory.MemoryOperationException;
import com.github.natanbc.ocmips.utils.MemoryUtils;
import li.cil.oc.api.machine.Machine;
import net.minecraft.nbt.NBTTagCompound;

import java.nio.IntBuffer;
import java.util.Arrays;

//struct eeprom_t {
// word sync;
// word dirty;
// word size;
// word data_size;
// word content[size];
// word data[data_size];
//}
public class EEPROMHandler implements MemoryHandler {
    private final Machine machine;
    private final String eepromAddress;
    private final int size;
    private final int dataSize;
    private int base;
    private CachedData content;
    private CachedData data;
    private boolean dirty;
    
    public EEPROMHandler(Machine machine, String eepromAddress, int size, int dataSize) {
        this.machine = machine;
        this.eepromAddress = eepromAddress;
        this.size = size;
        this.dataSize = dataSize;
    }

    public String getEepromAddress() {
        return eepromAddress;
    }

    public int getSize() {
        return size;
    }

    public int getDataSize() {
        return dataSize;
    }

    public void save(NBTTagCompound tag) {
        tag.setBoolean("dirty", dirty);
        if(dirty) {
            if(content != null) {
                tag.setByteArray("content", content.array);
            }
            if(data != null) {
                tag.setByteArray("data", data.array);
            }
        }
    }

    public void restore(NBTTagCompound tag) {
        dirty = tag.getBoolean("dirty");
        if(dirty) {
            if(tag.hasKey("content")) {
                content = new CachedData(tag.getByteArray("content"));
            }
            if(tag.hasKey("data")) {
                data = new CachedData(tag.getByteArray("data"));
            }
        }
    }

    @Override
    public void onAttach(MipsCPU cpu, int baseAddress) {
        this.base = baseAddress;
    }
    
    @Override
    public int read(MipsCPU cpu, int address) throws MemoryOperationException {
        if(address == base) {
            return 0;
        }
        if(address == base + 4) {
            return dirty ? 1 : 0;
        }
        if(address == base + 8) {
            return size;
        }
        if(address == base + 12) {
            return dataSize;
        }
        int offset = address - base - 16;
        if(offset < size) {
            return content(address).buffer.get(offset / 4);
        } else {
            return data(address).buffer.get((offset - size) / 4);
        }
    }
    
    @Override
    public void write(MipsCPU cpu, int address, int value) throws MemoryOperationException {
        if(address == base) {
            //mode = 0 -> reload changes (just dropping our caches is enough)
            //mode = 1 -> sync changes (write back to eeprom, if not dirty we do nothing)
            int mode = value & 1;
            if(mode == 0) {
                dirty = false;
                content = null;
                data = null;
            } else {
                if(!dirty) return;
                dirty = false;
                write(content, "set");
                write(data, "setData");
            }
            return;
        }
        //writing to dirty/size/dataSize does nothing
        if(address == base + 4 || address == base + 8 || address == base + 12) {
            return;
        }
        dirty = true;
        int offset = address - base - 12;
        if(offset < size) {
            content(address).buffer.put(offset / 4, value);
        } else {
            data(address).buffer.put((offset - size) / 4, value);
        }
    }
    
    @Override
    public int memorySize() {
        //sync word + dirty word + 2 size words + data
        return size + dataSize + 4 * 4;
    }
    
    private CachedData content(int address) throws MemoryOperationException {
        if(content == null) {
            content = new CachedData(withSize(read(address, "get"), size));
        }
        return content;
    }
    
    private CachedData data(int address) throws MemoryOperationException {
        if(data == null) {
            data = new CachedData(withSize(read(address, "getData"), dataSize));
        }
        return data;
    }
    
    private byte[] read(int address, String method) throws MemoryOperationException {
        try {
            return (byte[])machine.invoke(eepromAddress, method, new Object[0])[0];
        } catch(Exception e) {
            throw new MemoryOperationException(address, MemoryOperationException.Reason.ACCESS_ERROR);
        }
    }
    
    private void write(CachedData data, String method) throws MemoryOperationException {
        //if data is null it hasn't even been read, much less modified.
        if(data == null) return;
        try {
            machine.invoke(eepromAddress, method, new Object[] { data.array });
        } catch(Exception e) {
            throw new MemoryOperationException(base, MemoryOperationException.Reason.ACCESS_ERROR);
        }
    }

    private static byte[] withSize(byte[] array, int size) {
        if(array.length != size) {
            return Arrays.copyOf(array, size);
        }
        return array;
    }
    
    private static class CachedData {
        private final byte[] array;
        private final IntBuffer buffer;
    
        private CachedData(byte[] array) {
            this.array = array;
            this.buffer = MemoryUtils.wrapExact(array).asIntBuffer();
        }
    }
}
