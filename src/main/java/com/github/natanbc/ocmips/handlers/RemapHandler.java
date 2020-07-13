package com.github.natanbc.ocmips.handlers;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.memory.MemoryHandler;
import com.github.natanbc.mipscpu.memory.MemoryOperationException;

public class RemapHandler implements MemoryHandler {
    private final int target;
    private final int size;
    private int base;

    public RemapHandler(int target, int size) {
        this.target = target;
        this.size = size;
    }

    public int getTarget() {
        return target;
    }

    public int getSize() {
        return size;
    }

    @Override
    public void onAttach(MipsCPU cpu, int baseAddress) {
        this.base = baseAddress;
    }

    @Override
    public int read(MipsCPU cpu, int address) throws MemoryOperationException {
        return cpu.readWord(target + (address - base));
    }

    @Override
    public void write(MipsCPU cpu, int address, int value) throws MemoryOperationException {
        cpu.writeWord(target + (address - base), value);
    }

    @Override
    public int memorySize() {
        return size;
    }
}
