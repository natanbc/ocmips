package com.github.natanbc.mipscpu;

public class MipsRegisters {
    // mnemonics for integer registers
    public static final int
            ZERO =  0, AT =  1, V0 =  2, V1 =  3,
            A0 =    4, A1 =  5, A2 =  6, A3 =  7,
            T0 =    8, T1 =  9, T2 = 10, T3 = 11,
            T4 =   12, T5 = 13, T6 = 14, T7 = 15,
            S0 =   16, S1 = 17, S2 = 18, S3 = 19,
            S4 =   20, S5 = 21, S6 = 22, S7 = 23,
            T8 =   24, T9 = 25, K0 = 26, K1 = 27,
            GP =   28, SP = 29, FP = 30, RA = 31,
            //following registers can't be encoded directly
            PC =   32, LO = 33, HI = 34,
            RMW_ADDRESS = 35, RMW_VALID = 36;
    public static final int INTEGER_COUNT = 37;
    private static final String[] INTEGER_NAMES = {
            "$zero", "$at", "$v0", "$v1",
            "$a0", "$a1", "$a2", "$a3",
            "$t0", "$t1", "$t2", "$t3",
            "$t4", "$t5", "$t6", "$t7",
            "$s0", "$s1", "$s2", "$s3",
            "$s4", "$s5", "$s6", "$s7",
            "$t8", "$t9", "$k0", "$k1",
            "$gp", "$sp", "$fp", "$ra",
            "<pc>", "<lo>", "<hi>"
    };
    // mnemonics for floating point registers
    public static final int
            F0 =   0, F1 =   1, F2 =   2, F3 =   3,
            F4 =   4, F5 =   5, F6 =   6, F7 =   7,
            F8 =   8, F9 =   9, F10 = 10, F11 = 11,
            F12 = 12, F13 = 13, F14 = 14, F15 = 15,
            F16 = 16, F17 = 17, F18 = 18, F19 = 19,
            F20 = 20, F21 = 21, F22 = 22, F23 = 23,
            F24 = 24, F25 = 25, F26 = 26, F27 = 27,
            F28 = 28, F29 = 29, F30 = 30, F31 = 31,
            FP_CC = 32;
    public static final int FLOAT_COUNT = 33;
    private static final String[] FLOAT_NAMES = {
            "$f0",  "$f1",  "$f2",  "$f3",
            "$f4",  "$f5",  "$f6",  "$f7",
            "$f8",  "$f9",  "$f10", "$f11",
            "$f12", "$f13", "$f14", "$f15",
            "$f16", "$f17", "$f18", "$f19",
            "$f20", "$f21", "$f22", "$f23",
            "$f24", "$f25", "$f26", "$f27",
            "$f28", "$f29", "$f30", "$f31",
            "<fp_cc>"
    };
    // mnemonics for coprocessor 0 (only partially implemented)
    public static final int
            C0_VADDR = 8, C0_STATUS = 12, C0_CAUSE = 13,
            C0_EPC = 14, C0_EXHPC = 10; /*exception handler */
    public static final int COP0_COUNT = 15;
    public static final int
            STATUS_INTERRUPT_ENABLED = 0,
            STATUS_EXCEPTION_LEVEL = 1,
            STATUS_USER_MODE = 4;

    // used by interpreter to detect writes to $pc
    public static final int FLAG_PC_WRITTEN = 0x01;

    private final int[] integerRegisters = new int[INTEGER_COUNT];
    private final int[] fpRegisters = new int[FLOAT_COUNT];
    private final int[] cop0 = new int[COP0_COUNT];
    private int flags = 0;

    public MipsRegisters() {
        /* bits 15..8 are the interrupt mask */
        /* bit 4 is the user mode (always 1) */
        /* bit 1 is the exception level */
        /* bit 0 is interrupt enable */
        writeInteger(C0_STATUS, 0x0000ff11);
    }

    public void clearFlags() {
        flags &= ~FLAG_PC_WRITTEN;
    }

    public boolean wasPcWritten() {
        return (flags & FLAG_PC_WRITTEN) == FLAG_PC_WRITTEN;
    }

    public int readInteger(int register) {
        if(register == ZERO) return 0;
        return integerRegisters[register];
    }

    public void writeInteger(int register, int value) {
        if(register == ZERO) return;
        if(register == PC) {
           flags |= FLAG_PC_WRITTEN;
        }
        integerRegisters[register] = value;
    }

    public int readFloat(int register) {
        return fpRegisters[register];
    }

    public void writeFloat(int register, int value) {
        fpRegisters[register] = value;
    }

    public int readCop0(int register) {
        return cop0[register];
    }

    public void writeCop0(int register, int value) {
        if(register == C0_STATUS) {
            //always set the user mode bit
            value |= 1 << STATUS_USER_MODE;
        }
        cop0[register] = value;
    }

    public boolean readStatusBit(int bit) {
        return ((readCop0(C0_STATUS) >> bit) & 0b1) == 0b1;
    }

    public void writeStatusBit(int bit, boolean value) {
        int s = readCop0(C0_STATUS);
        s ^= (-(value ? 1 : 0) ^ s) & (1 << bit);
        writeCop0(C0_STATUS, s);
    }

    public boolean readFloatCondition(int cc) {
        return ((fpRegisters[FP_CC] >> cc) & 0b1) == 0b1;
    }

    public void writeFloatCondition(int cc, boolean value) {
        int v = fpRegisters[FP_CC];
        //change nth bit to x
        //number ^= (-x ^ number) & (1UL << n);
        v ^= (-(value ? 1 : 0) ^ v) & (1 << cc);
        fpRegisters[FP_CC] = v;
    }

    public void startAtomicUpdate(int address) {
        writeInteger(RMW_ADDRESS, address);
        writeInteger(RMW_VALID, 1);
    }

    public boolean isAtomicUpdateValid(int address) {
        return readInteger(RMW_VALID) != 0 && readInteger(RMW_ADDRESS) == address;
    }

    public void endAtomicUpdate() {
        writeInteger(RMW_ADDRESS, 0);
        writeInteger(RMW_VALID, 0);
    }

    public static String integerName(int reg) {
        if(reg >= 0 && reg < INTEGER_NAMES.length) {
            return INTEGER_NAMES[reg];
        }
        return "<invalid>";
    }

    public static String floatName(int reg) {
        if(reg >= 0 && reg < FLOAT_NAMES.length) {
            return FLOAT_NAMES[reg];
        }
        return "<invalid>";
    }
}
