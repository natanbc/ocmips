package com.github.natanbc.mipscpu.instruction;

import com.github.natanbc.mipscpu.MipsException;

public class TrapException extends MipsException {
    private final Cause cause;

    public TrapException(Cause cause, String message) {
        super(errorMessage(cause, message));
        this.cause = cause;
    }

    public TrapException(Cause cause) {
        this(cause, null);
    }

    /* getCause() is already declared in Throwable */
    public Cause getTrapCause() {
        return cause;
    }

    private static String errorMessage(Cause cause, String message) {
        if(message == null) return cause.toString();
        return cause + ": " + message;
    }

    public enum Cause {
        Int(0, "Hardware interrupt"),
        AdEL(4, "Address Error (Load or instruction fetch)"),
        AdES(5, "Address Error (Store)"),
        IBE(5, "Instruction fetch Bus Error"),
        DBE(7, "Data load or store Bus Error"),
        Sys(8, "Syscall"),
        Bp(9, "Breakpoint"),
        RI(10, "Reserved Instruction"),
        CpU(11, "Coprocessor Unimplemented"),
        Ov(12, "Arithmetic Overflow"),
        Tr(13, "Trap"), /* haha astolfo go brrr */
        FPE(14, "Floating Point exception");

        private final int code;
        private final String description;

        Cause(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static Cause fromCode(int code) {
            for(Cause c : values()) {
                if(c.code == code) return c;
            }
            return null;
        }
    }
}
