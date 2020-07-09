package com.github.natanbc.mipscpu.instruction;

import com.github.natanbc.mipscpu.MipsException;

public class InstructionExecutionException extends MipsException {
    public InstructionExecutionException(String s) {
        super(s);
    }

    public InstructionExecutionException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
