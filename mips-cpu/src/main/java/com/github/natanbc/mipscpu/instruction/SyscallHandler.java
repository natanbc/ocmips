package com.github.natanbc.mipscpu.instruction;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.MipsException;

public interface SyscallHandler {
    /**
     * Called when a syscall instruction is found.
     *
     * @param cpu The cpu that triggered the syscall.
     */
    void handleSyscall(MipsCPU cpu) throws MipsException;
}
