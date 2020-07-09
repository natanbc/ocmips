package com.github.natanbc.mipscpu.memory;

import com.github.natanbc.mipscpu.MipsException;

public class IllegalAddressException extends MipsException {
    public enum Reason {
        NOT_MAPPED, READ_ONLY, ACCESS_ERROR
    }

    private final int address;
    private final Reason reason;

    public IllegalAddressException(int address, Reason reason, Throwable cause) {
        super("Invalid access to address 0x" + Integer.toHexString(address) + ": " + reason, cause);
        this.address = address;
        this.reason = reason;
    }

    public IllegalAddressException(int address, Reason reason) {
        this(address, reason, null);
    }

    public int getAddress() {
        return address;
    }

    public Reason getReason() {
        return reason;
    }
}
