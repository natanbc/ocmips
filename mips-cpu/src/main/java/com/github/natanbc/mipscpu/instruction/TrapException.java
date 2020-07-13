package com.github.natanbc.mipscpu.instruction;

import com.github.natanbc.mipscpu.MipsException;

public class TrapException extends MipsException {
    private final int trappingInstruction;
    private final int code;

    public TrapException(int trappingInstruction, int code) {
        super("Trapped at 0x" + Integer.toHexString(trappingInstruction) +
                " (" + MipsInstruction.toString(trappingInstruction) + ") " + ": " + code);
        this.trappingInstruction = trappingInstruction;
        this.code = code;
    }

    public int getTrappingInstruction() {
        return trappingInstruction;
    }

    public int getCode() {
        return code;
    }
}
