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
            PC =   32, LO = 33, HI = 34;
    public static final int INTEGER_COUNT = 35;
    // mnemonics for floating point registers
    public static final int
            F0 =   0, F1 =   1, F2 =   2, F3 =   3,
            F4 =   4, F5 =   5, F6 =   6, F7 =   7,
            F8 =   8, F9 =   9, F10 = 10, F11 = 11,
            F12 = 12, F13 = 13, F14 = 14, F15 = 15,
            F16 = 16, F17 = 17, F18 = 18, F19 = 19,
            F20 = 20, F21 = 21, F22 = 22, F23 = 23,
            F24 = 24, F25 = 25, F26 = 26, F27 = 27,
            F28 = 28, F29 = 29, F30 = 30, F31 = 31;
    public static final int FLOAT_COUNT = 32;

    // used by interpreter to detect writes to $pc
    public static final int FLAG_PC_WRITTEN = 0x01;

    private final int[] integerRegisters = new int[INTEGER_COUNT];
    private final float[] fpRegisters = new float[FLOAT_COUNT];
    private int flags = 0;

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

    public float readFloat(int register) {
        return fpRegisters[register];
    }

    public void writeFloat(int register, float value) {
        fpRegisters[register] = value;
    }
}
