package com.github.natanbc.ocmips.handlers;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.MipsException;
import com.github.natanbc.mipscpu.memory.MemoryHandler;

public interface CleanableHandler extends MemoryHandler {
    void cleanup(MipsCPU cpu) throws MipsException;
}
