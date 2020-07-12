package com.github.natanbc.mipscpu.instruction;

import com.github.natanbc.mipscpu.MipsException;

public class TrapException extends MipsException {
    private final MipsInstruction trappingInstruction;
    private final int code;

    public TrapException(MipsInstruction trappingInstruction, int code) {
        super("Trapped at " + trappingInstruction + ": " + code);
        this.trappingInstruction = trappingInstruction;
        this.code = code;
    }

    public MipsInstruction getTrappingInstruction() {
        return trappingInstruction;
    }

    public int getCode() {
        return code;
    }
}
