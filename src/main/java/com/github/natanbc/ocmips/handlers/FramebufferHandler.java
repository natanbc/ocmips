package com.github.natanbc.ocmips.handlers;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.MipsException;
import com.github.natanbc.mipscpu.instruction.InstructionExecutionException;
import com.github.natanbc.mipscpu.memory.MemoryOperationException;
import com.github.natanbc.ocmips.RetryInNextTick;
import com.github.natanbc.ocmips.handlers.internal.FramebufferRenderer;
import li.cil.oc.api.machine.LimitReachedException;
import li.cil.oc.api.machine.Machine;

// struct framebuffer_t { word sync; word width; word height; word vram[width * height]; }
public class FramebufferHandler implements CleanableHandler {
    public static final int SYNC_BITBLT = 1, SYNC_CLEAR = 2;

    private final Machine machine;
    private final String gpuAddress;
    private final int width;
    private final int height;
    private final int bufferNumber;
    private final FramebufferRenderer renderer;
    private int base;

    public FramebufferHandler(Machine machine, String gpuAddress, int w, int h, int bufferNumber) {
        this.machine = machine;
        this.gpuAddress = gpuAddress;
        this.width = w;
        this.height = h;
        this.bufferNumber = bufferNumber;
        this.renderer = FramebufferRenderer.create(machine, gpuAddress, bufferNumber, w, h);
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
            if(value == SYNC_BITBLT) {
                try {
                    machine.invoke(gpuAddress, "bitblt", new Object[0]);
                } catch(LimitReachedException e) {
                    throw new RetryInNextTick(e);
                } catch (Exception e) {
                    throw new MemoryOperationException(address, MemoryOperationException.Reason.ACCESS_ERROR);
                }
            } else if(value == SYNC_CLEAR) {
                try {
                    renderer.clear();
                } catch (LimitReachedException e) {
                    throw new RetryInNextTick(e);
                } catch (Exception e) {
                    throw new MemoryOperationException(address, MemoryOperationException.Reason.ACCESS_ERROR);
                }
            } else {
                throw new MemoryOperationException(address, MemoryOperationException.Reason.INVALID_VALUE);
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
            renderer.set(x, y, value);
        } catch (Exception e) {
            throw new MemoryOperationException(address, MemoryOperationException.Reason.ACCESS_ERROR);
        }
    }

    @Override
    public int memorySize() {
        return (width * height + 3) * 4;
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
