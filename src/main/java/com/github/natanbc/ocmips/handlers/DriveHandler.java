package com.github.natanbc.ocmips.handlers;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.memory.MemoryHandler;
import com.github.natanbc.mipscpu.memory.MemoryOperationException;
import com.github.natanbc.ocmips.utils.MemoryUtils;
import li.cil.oc.api.machine.Machine;
import net.minecraft.nbt.NBTTagCompound;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

//struct drive {
// word sync;
// word mode;
// word size;
// word sector_size;
// word current_sector;
// word dirty;
// word data[size / 4];
//}
public class DriveHandler implements MemoryHandler {
    private static final int
            MODE_DEFAULT = 0,    // only write when sync is written to
            MODE_AUTO_WRITE = 1, // auto write when changing current sector
            MODE_READ_ONLY = 2;  // no writes at all

    private final Machine machine;
    private final String driveAddress;
    private final int mode;
    private final int size;
    private final int sectorSize;
    private int base;
    private int currentSector = -1;
    private byte[] currentSectorData;
    private IntBuffer currentSectorBuffer;
    private boolean dirty;

    public DriveHandler(Machine machine, String driveAddress, int mode, int size, int sectorSize) {
        this.machine = machine;
        this.driveAddress = driveAddress;
        this.mode = mode;
        this.size = size;
        this.sectorSize = sectorSize;
    }

    public String getDriveAddress() {
        return driveAddress;
    }

    public int getMode() {
        return mode;
    }

    public int getSize() {
        return size;
    }

    public int getSectorSize() {
        return sectorSize;
    }

    public void save(NBTTagCompound tag) {
        tag.setBoolean("dirty", dirty);
        tag.setInteger("current_sector", currentSector);
        if(dirty) {
            tag.setByteArray("current_data", currentSectorData);
        }
    }

    public void restore(NBTTagCompound tag) {
        dirty = tag.getBoolean("dirty");
        currentSector = tag.getInteger("current_sector");
        if(tag.hasKey("current_data")) {
            currentSectorData = tag.getByteArray("current_data");
            currentSectorBuffer = MemoryUtils.wrapExact(currentSectorData).asIntBuffer();
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
            return mode;
        }
        if(address == base + 8) {
            return size;
        }
        if(address == base + 12) {
            return sectorSize;
        }
        if(address == base + 16) {
            return currentSector;
        }
        if(address == base + 20) {
            return dirty ? 0 : 1;
        }
        int position = address - base - 24;
        int sector = position / sectorSize;
        seek(address, sector);
        return currentSectorBuffer.get((position - sector * sectorSize) / 4);
    }

    @Override
    public void write(MipsCPU cpu, int address, int value) throws MemoryOperationException {
        if(address == base) {
            if(value == 0) {
                dirty = false;
                //this is enough to trigger a reload on the next read
                currentSector = -1;
            } else {
                if(!dirty) return;
                dirty = false;
                flush(address);
            }
            return;
        }
        if(address == base + 4 || address == base + 8 || address == base + 12 ||
            address == base + 16 || address == base + 20) {
            throw new MemoryOperationException(address, MemoryOperationException.Reason.READ_ONLY);
        }
        if(mode == MODE_READ_ONLY) {
            throw new MemoryOperationException(address, MemoryOperationException.Reason.READ_ONLY);
        }
        int position = address - base - 24;
        int sector = position / sectorSize;
        seek(address, sector);
        currentSectorBuffer.put(position - sector * sectorSize, value);
        dirty = true;
    }

    @Override
    public int memorySize() {
        return size + 6 * 4;
    }

    private void seek(int address, int sector) throws MemoryOperationException {
        if(sector != currentSector) {
            if(dirty && mode == MODE_AUTO_WRITE) {
                flush(address);
            }
            byte[] data;
            try {
                data = (byte[])machine.invoke(driveAddress, "readSector", new Object[] { sector + 1 })[0];
            } catch (Exception e) {
                throw new MemoryOperationException(address, MemoryOperationException.Reason.ACCESS_ERROR);
            }
            if(MemoryUtils.isExactWordCount(data.length)) {
                currentSectorData = data;
                currentSectorBuffer = MemoryUtils.wrapExact(data).asIntBuffer();
            } else {
                ByteBuffer bb = MemoryUtils.allocateRounding(data.length);
                bb.put(data).position(0);
                currentSectorData = bb.array();
                currentSectorBuffer = bb.asIntBuffer();
            }
            currentSector = sector;
        }
    }

    private void flush(int address) throws MemoryOperationException {
        try {
            machine.invoke(driveAddress, "writeSector", new Object[] { currentSector + 1, currentSectorData });
        } catch (Exception e) {
            throw new MemoryOperationException(address, MemoryOperationException.Reason.ACCESS_ERROR);
        }
    }
}
