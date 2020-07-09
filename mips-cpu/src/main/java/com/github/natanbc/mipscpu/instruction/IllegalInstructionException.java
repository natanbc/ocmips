package com.github.natanbc.mipscpu.instruction;

import com.github.natanbc.mipscpu.MipsException;

public class IllegalInstructionException extends MipsException {
    private final int encoding;
    private final String reason;

    public IllegalInstructionException(int encoding, String reason) {
        super("Invalid instruction 0x" + Integer.toHexString(encoding) + ": " + reason);
        this.encoding = encoding;
        this.reason = reason;
    }

    public int getEncoding() {
        return encoding;
    }

    public String getReason() {
        return reason;
    }
}
