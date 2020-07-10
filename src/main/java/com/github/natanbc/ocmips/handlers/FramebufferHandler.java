package com.github.natanbc.ocmips.handlers;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.MipsException;
import com.github.natanbc.mipscpu.instruction.InstructionExecutionException;
import com.github.natanbc.mipscpu.memory.MemoryOperationException;
import com.github.natanbc.ocmips.RetryInNextTick;
import li.cil.oc.api.machine.LimitReachedException;
import li.cil.oc.api.machine.Machine;

// struct framebuffer_t { word sync; word width; word height; word vram[width * height]; }
public class FramebufferHandler implements CleanableHandler {
    private final Machine machine;
    private final String gpuAddress;
    private final int width;
    private final int height;
    private final int bufferNumber;
    private int base;
    private int background;

    public FramebufferHandler(Machine machine, String gpuAddress, int w, int h, int bufferNumber) {
        this.machine = machine;
        this.gpuAddress = gpuAddress;
        this.width = w;
        this.height = h;
        this.bufferNumber = bufferNumber;
    }

    public String getGpuAddress() {
        return gpuAddress;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBufferNumber() {
        return bufferNumber;
    }

    @Override
    public void onAttach(MipsCPU cpu, int baseAddress) {
        this.base = baseAddress;
    }

    @Override
    public int read(MipsCPU cpu, int address) throws MemoryOperationException {
        if(address == base) return 0;
        if(address == base + 4) return width;
        if(address == base + 8) return height;
        int index = (address - base - 12) / 4;
        int x = index % width;
        int y = index / width;
        try {
            Object[] pixel = machine.invoke(gpuAddress, "get", new Object[] { x + 1, y + 1 });
            return (Integer)pixel[2];
        } catch (Exception e) {
            throw new MemoryOperationException(address, MemoryOperationException.Reason.ACCESS_ERROR);
        }
    }

    @Override
    public void write(MipsCPU cpu, int address, int value) throws MemoryOperationException {
        if(address == base) {
            try {
                machine.invoke(gpuAddress, "bitblt", new Object[0]);
            } catch(LimitReachedException e) {
                throw new RetryInNextTick();
            } catch (Exception e) {
                throw new MemoryOperationException(address, MemoryOperationException.Reason.ACCESS_ERROR);
            }
            return;
        }
        if(address == base + 4 || address == base + 8) {
            throw new MemoryOperationException(address, MemoryOperationException.Reason.READ_ONLY);
        }
        int index = (address - base - 12) / 4;
        int x = index % width;
        int y = index / width;
        try {
            if(background != value) {
                machine.invoke(gpuAddress, "setBackground", new Object[] { value });
                background = value;
            }
            machine.invoke(gpuAddress, "set", new Object[] {
                    x + 1,
                    y + 1,
                    " "
            });
        } catch (Exception e) {
            throw new MemoryOperationException(address, MemoryOperationException.Reason.ACCESS_ERROR);
        }
    }

    @Override
    public int memorySize() {
        return (width * height + 1) * 4;
    }

    @Override
    public void cleanup(MipsCPU cpu) throws MipsException {
        try {
            machine.invoke(gpuAddress, "freeBuffer", new Object[] {
                    bufferNumber
            });
        } catch (Exception e) {
            throw new InstructionExecutionException("Framebuffer tear down failed");
        }
    }
}
