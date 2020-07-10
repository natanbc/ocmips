package com.github.natanbc.ocmips.handlers;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.memory.MemoryHandler;
import com.github.natanbc.mipscpu.memory.MemoryOperationException;
import com.github.natanbc.ocmips.OCMips;

public class BadAppleHandler implements MemoryHandler {
    private int base;

    @Override
    public void onAttach(MipsCPU cpu, int baseAddress) {
        this.base = baseAddress;
    }

    @Override
    public int read(MipsCPU cpu, int address) {
        return OCMips.badAppleBuffer.get((address - base) / 4);
    }

    @Override
    public void write(MipsCPU cpu, int address, int value) throws MemoryOperationException {
        throw new MemoryOperationException(address, MemoryOperationException.Reason.READ_ONLY);
    }

    @Override
    public int memorySize() {
        return OCMips.badAppleBuffer.capacity() * 4;
    }
}
