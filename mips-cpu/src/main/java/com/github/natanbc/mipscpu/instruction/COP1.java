package com.github.natanbc.mipscpu.instruction;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.MipsException;

import static com.github.natanbc.mipscpu.MipsRegisters.*;
import static com.github.natanbc.mipscpu.instruction.MipsInstruction.*;
import static com.github.natanbc.mipscpu.instruction.TrapException.Cause.*;

class COP1 {
    private static final String[] COMPARE_CONDITIONS = {
            "f",  "un",   "eq",  "ueq", "olt", "ult", "ole", "ule",
            "sf", "ngle", "seq", "ngl", "lt",  "nge", "le",  "ngt"
    };

    //i fear no man
    //but that thing
    //it scares me
    static void execute(MipsCPU cpu, int instruction) throws MipsException {
        //https://github.com/iamgreaser/ocmips/blob/4f9c3443c2195b41252845d420ed461807add0df/src/main/java/potato/chocolate/mods/ebola/arch/mips/Jipsy.java#L1216
        int rs = (instruction>>>21)&0x1F;
        int rt = (instruction>>>16)&0x1F;
        int ft = rt;
        int rd = (instruction>>>11)&0x1F;
        int fs = rd;
        int fd = (instruction>>>6)&0x1F;
        int otyp1 = (instruction)&0x3F;
        if(rs >= 16) {
            switch (rs) {
                //SP
                case 16: {
                    float vs1 = Float.intBitsToFloat(cpu.registers().readFloat(fs));
                    float vs2 = Float.intBitsToFloat(cpu.registers().readFloat(ft));
                    //c.<cond>.s
                    if((otyp1 & 0b110000) == 0b110000) {
                        int cc = (instruction>>8)&0b111;
                        int cond = otyp1 & 0b1111;
                        /*
                        In the cond field of the instruction: cond2..1 specify the nature of the
                        comparison (equals, less than, and so on). cond0 specifies whether the
                        comparison is ordered or unordered, that is, false or true if any operand
                        is a NaN; cond3 indicates whether the instruction should signal an exception
                        on QNaN inputs, or not (see Table 3.2).
                         */
                        boolean trapQNaN = (cond & 0b1000) == 0b1000;
                        boolean unorderedCompare = (cond & 0b1) == 0b1;
                        boolean unordered = Float.isNaN(vs1) | Float.isNaN(vs2);
                        if(trapQNaN & unordered) {
                            throw new TrapException(FPE);
                        }
                        boolean a;
                        switch ((cond & 0b110) >> 1) {
                            // F   / SF
                            // UN  / NGLE
                            case 0: {
                                a = false;
                                break;
                            }
                            // EQ  / SEQ
                            // UEQ / NGL
                            case 1: {
                                a = vs1 == vs2;
                                break;
                            }
                            // OLT / LT
                            // ULT / NGE
                            case 2: {
                                a = vs1 < vs2;
                                break;
                            }
                            // OLE / LE
                            // ULE / NGT
                            case 3: {
                                a = vs1 <= vs2;
                                break;
                            }
                            default: throw new AssertionError();
                        }
                        cpu.registers().writeFloatCondition(cc, a | (unorderedCompare & unordered));
                    } else {
                        float vd = 0.0f;
                        switch (otyp1) {
                            // ADD
                            case 0b000000: vd = vs1 + vs2; break;
                            // SUB
                            case 0b000001: vd = vs1 - vs2; break;
                            // MUL
                            case 0b000010: vd = vs1 * vs2; break;
                            // DIV
                            case 0b000011: vd = vs1 / vs2; break;
                            // SQRT.S
                            case 0b000100: vd = (float)Math.sqrt(vs1); break;
                            // ABS
                            case 0b000101:  vd = Math.abs(vs1); break;
                            // MOV
                            case 0b000110: vd = vs1; break;
                            // NEG
                            case 0b000111: vd = -vs1; break;
                            // CEIL.W.S
                            case 0b001110: {
                                cpu.registers().writeFloat(fd, truncToFixed(Math.ceil(vs1)));
                                fd = -1;
                                break;
                            }
                            // FLOOR.W.S
                            case 0b001111: {
                                cpu.registers().writeFloat(fd, truncToFixed(Math.floor(vs1)));
                                fd = -1;
                                break;
                            }
                            // ROUND.W.S
                            case 0b001100: {
                                cpu.registers().writeFloat(fd, truncToFixed(Math.round(vs1)));
                                fd = -1;
                                break;
                            }
                            // TRUNC.W.S
                            case 0b001101: {
                                cpu.registers().writeFloat(fd, truncToFixed(vs1));
                                fd = -1;
                                break;
                            }
                            // CVT from S to D
                            case 0b100001: {
                                long vdi = Double.doubleToLongBits(vs1);
                                fd &= ~1;
                                cpu.registers().writeFloat(fd, (int)(vdi>>>32));
                                cpu.registers().writeFloat(fd + 1, (int)(vdi));
                                fd = -1;
                                break;
                            }
                            // CVT from S to W
                            case 0b100100: {
                                cpu.registers().writeFloat(fd, (int)vs1);
                                fd = -1;
                                break;
                            }
                            default: throw new TrapException(RI, "Unimplemented SP COP1");
                        }
                        if(fd != -1) {
                            cpu.registers().writeFloat(fd, Float.floatToIntBits(vd));
                        }
                    }
                    break;
                }
                //DP
                case 17: {
                    fs &= ~1; ft &= ~1;
                    double vs1 = long2double(cpu.registers().readFloat(fs), cpu.registers().readFloat(fs + 1));
                    double vs2 = long2double(cpu.registers().readFloat(ft), cpu.registers().readFloat(ft + 1));
                    //c.<cond>.d
                    if((otyp1 & 0b110000) == 0b110000) {
                        int cc = (instruction>>8)&0b111;
                        int cond = otyp1 & 0b1111;
                        /*
                        In the cond field of the instruction: cond2..1 specify the nature of the
                        comparison (equals, less than, and so on). cond0 specifies whether the
                        comparison is ordered or unordered, that is, false or true if any operand
                        is a NaN; cond3 indicates whether the instruction should signal an exception
                        on QNaN inputs, or not (see Table 3.2).
                         */
                        boolean trapQNaN = (cond & 0b1000) == 0b1000;
                        boolean unorderedCompare = (cond & 0b1) == 0b1;
                        boolean unordered = Double.isNaN(vs1) | Double.isNaN(vs2);
                        if(trapQNaN & unordered) {
                            throw new TrapException(FPE);
                        }
                        boolean a;
                        switch ((cond & 0b110) >> 1) {
                            // F   / SF
                            // UN  / NGLE
                            case 0: {
                                a = false;
                                break;
                            }
                            // EQ  / SEQ
                            // UEQ / NGL
                            case 1: {
                                a = vs1 == vs2;
                                break;
                            }
                            // OLT / LT
                            // ULT / NGE
                            case 2: {
                                a = vs1 < vs2;
                                break;
                            }
                            // OLE / LE
                            // ULE / NGT
                            case 3: {
                                a = vs1 <= vs2;
                                break;
                            }
                            default: throw new AssertionError();
                        }
                        cpu.registers().writeFloatCondition(cc, a | (unorderedCompare & unordered));
                    } else {
                        double vd = 0;
                        switch (otyp1) {
                            // ADD
                            case 0b000000: vd = vs1 + vs2; break;
                            // SUB
                            case 0b000001: vd = vs1 - vs2; break;
                            // MUL
                            case 0b000010: vd = vs1 * vs2; break;
                            // DIV
                            case 0b000011: vd = vs1 / vs2; break;
                            // SQRT.D
                            case 0b000100: vd = Math.sqrt(vs1); break;
                            // ABS
                            case 0b000101: vd = Math.abs(vs1); break;
                            // MOV
                            case 0b000110: vd = vs1; break;
                            // NEG
                            case 0b000111: vd = -vs1; break;
                            // CEIL.W.D
                            case 0b001110: {
                                cpu.registers().writeFloat(fd, truncToFixed(Math.ceil(vs1)));
                                fd = -1;
                                break;
                            }
                            // FLOOR.W.D
                            case 0b001111: {
                                cpu.registers().writeFloat(fd, truncToFixed(Math.floor(vs1)));
                                fd = -1;
                                break;
                            }
                            // ROUND.W.D
                            case 0b001100: {
                                cpu.registers().writeFloat(fd, truncToFixed(Math.round(vs1)));
                                fd = -1;
                                break;
                            }
                            // TRUNC.W.D
                            case 0b001101: {
                                cpu.registers().writeFloat(fd, truncToFixed(vs1));
                                fd = -1;
                                break;
                            }
                            // CVT from D to S
                            case 0b100000: {
                                cpu.registers().writeFloat(fd, Float.floatToIntBits((float)vs1));
                                fd = -1;
                                break;
                            }
                            // CVT from D to W
                            case 0b100100: {
                                cpu.registers().writeFloat(fd, (int)vs1);
                                fd = -1;
                                break;
                            }
                            default: throw new TrapException(RI, "Unimplemented DP COP1");
                        }
                        if (fd != -1) {
                            long vdi = Double.doubleToLongBits(vd);
                            fd &= ~1;
                            cpu.registers().writeFloat(fd, (int)(vdi>>>32));
                            cpu.registers().writeFloat(fd+1, (int)(vdi));
                        }
                    }
                    break;
                }
                // W
                case 20: {
                    switch (otyp1) {
                        // CVT from W to S
                        case 32: {
                            cpu.registers().writeFloat(fd, Float.floatToIntBits(cpu.registers().readFloat(fs)));
                            break;
                        }
                        // CVT from W to D
                        case 33: {
                            long vdi = Double.doubleToLongBits(cpu.registers().readFloat(fs));
                            fd &= ~1;
                            cpu.registers().writeFloat(fd, (int)(vdi));
                            cpu.registers().writeFloat(fd+1, (int)(vdi>>>32));
                            break;
                        }
                        default: throw new TrapException(RI, "Unimplemented W COP1");
                    }
                    break;
                }
                default: throw new TrapException(RI, "Unknown COP1 format");
            }
        } else {
            switch (rs) {
                // MFC1
                case 0b00000: {
                    cpu.registers().writeInteger(rt, cpu.registers().readFloat(rd));
                    break;
                }
                // CFC1
                case 0b00010: {
                    int tmp;
                    // FCR0: Revision
                    if (rd == 0) {
                        tmp = 0x0300;
                    } else {
                        throw new TrapException(RI, "Unimplemented COP1 CFCn");
                    }
                    cpu.registers().writeInteger(rt, tmp);
                    break;
                }
                // MTC1
                case 0b00100: {
                    cpu.registers().writeFloat(fs, cpu.registers().readInteger(rt));
                    break;
                }
                // CTC1
                case 0b00110: {
                    throw new TrapException(RI, "Unimplemented COP1 CTCn");
                }
                // BC1F / BC1T
                case 0b01000: {
                    int cc = (instruction >>> 18) & 0b111;
                    int offset = instruction & 0xFFFF;
                    boolean tf = ((instruction >>> 16) & 0b1) == 0b1;
                    if(cpu.registers().readFloatCondition(cc) == tf) {
                        cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + 4 + (signExtend(offset) << 2));
                    }
                    break;
                }
                default: {
                    throw new TrapException(RI, "Unimplemented COP1 instruction");
                }
            }
        }
    }

    static String toString(int instruction) {
        int rs = (instruction>>>21)&0x1F;
        int rt = (instruction>>>16)&0x1F;
        @SuppressWarnings("UnnecessaryLocalVariable") int ft = rt;
        int rd = (instruction>>>11)&0x1F;
        @SuppressWarnings("UnnecessaryLocalVariable") int fs = rd;
        int fd = (instruction>>>6)&0x1F;
        int otyp1 = (instruction)&0x3F;
        if(rs >= 16) {
            switch (rs) {
                //SP
                case 0b10000: {
                    if((otyp1 & 0b110000) == 0b110000) {
                        int cc = (instruction>>8)&0b111;
                        int cond = otyp1 & 0b1111;
                        return "c." + COMPARE_CONDITIONS[cond] + ".s " + cc + ", " + fregs(fs, ft);
                    }
                    switch (otyp1) {
                        case 0b000000: return "add.s " + fregs(fd, fs, ft);
                        case 0b000001: return "sub.s " + fregs(fd, fs, ft);
                        case 0b000010: return "mul.s " + fregs(fd, fs, ft);
                        case 0b000011: return "div.s " + fregs(fd, fs, ft);
                        case 0b000100: return "sqrt.s " + fregs(fd, fs);
                        case 0b000101: return "abs.s " + fregs(fd, fs);
                        case 0b000110: return "mov.s " + fregs(fd, fs);
                        case 0b000111: return "neg.s " + fregs(fd, fs);
                        case 0b001110: return "ceil.w.s " + fregs(fd, fs);
                        case 0b001111: return "floor.w.s " + fregs(fd, fs);
                        case 0b001100: return "round.w.s " + fregs(fd, fs);
                        case 0b001101: return "trunc.w.s " + fregs(fd, fs);
                        case 0b100001: return "cvt.d.s " + fregs(fd & ~1, fs);
                        case 0b100100: return "cvt.w.s " + fregs(fd, fs);
                        default: return invalid(instruction, "Unimplemented SP COP1");
                    }
                }
                //DP
                case 0b10001: {
                    if((otyp1 & 0b110000) == 0b110000) {
                        int cc = (instruction>>8)&0b111;
                        int cond = otyp1 & 0b1111;
                        return "c." + COMPARE_CONDITIONS[cond] + ".d " + cc + ", " + fregs(fs, ft);
                    }
                    switch (otyp1) {
                        case 0b000000: return "add.d " + fregs(fd, fs, ft);
                        case 0b000001: return "sub.d " + fregs(fd, fs, ft);
                        case 0b000010: return "mul.d " + fregs(fd, fs, ft);
                        case 0b000011: return "div.d " + fregs(fd, fs, ft);
                        case 0b000100: return "sqrt.d " + fregs(fd, fs);
                        case 0b000101: return "abs.d " + fregs(fd, fs);
                        case 0b000110: return "mov.d " + fregs(fd, fs);
                        case 0b000111: return "neg.d " + fregs(fd, fs);
                        case 0b001110: return "ceil.w.d " + fregs(fd, fs);
                        case 0b001111: return "floor.w.d " + fregs(fd, fs);
                        case 0b001100: return "round.w.d " + fregs(fd, fs);
                        case 0b001101: return "trunc.w.d " + fregs(fd, fs);
                        case 0b100000: return "cvt.s.d " + fregs(fd, fs);
                        case 0b100100: return "cvt.w.d " + fregs(fd, fs);
                        default: return invalid(instruction, "Unimplemented DP COP1");
                    }
                }
                // W
                case 0b10100: {
                    switch (otyp1) {
                        case 0b100000: return "cvt.s.w " + fregs(fd, fs);
                        case 0b100001: return "cvt.d.w " + fregs(fd, fs);
                        default: return invalid(instruction, "Unimplemented W COP1");
                    }
                }
                default: return invalid(instruction, "Unknown COP1 size");
            }
        } else {
            switch (rs) {
                // MFC1
                case 0b00000: return "mfc1 " + ir(rt) + ", " + fr(fs);
                // CFC1
                case 0b00010: {
                    // FCR0: Revision
                    if (rd == 0) {
                        return "cfc1 " + ir(rt) + ", FIR";
                    } else {
                        return invalid(instruction, "Unimplemented COP1 CFCn");
                    }
                }
                // MTC1
                case 0b00100: return "mtc1 " + fr(fs) + ", " + ir(rt);
                // CTC1
                case 0b00110: {
                    return invalid(instruction, "Unimplemented COP1 CTCn");
                }
                // BC1F / BC1T
                case 0b01000: {
                    int cc = (instruction >>> 18) & 0b111;
                    int offset = instruction & 0xFFFF;
                    boolean tf = ((instruction >>> 16) & 0b1) == 0b1;
                    return (tf ? "bc1t" : "bc1f") + " " + cc + ", " + signExtend(offset);
                }
                default: {
                    return invalid(instruction, "Unimplemented COP1");
                }
            }
        }
    }

    private static double long2double(int upper, int lower) {
        long l = ((upper & 0xFFFFFFFFL) << 32) | (lower & 0xFFFFFFFFL);
        return Double.longBitsToDouble(l);
    }

    // if the value isn't in the range [-2^31, 2^31 - 1], returns 2^31 - 1
    private static int truncToFixed(double val) {
        if(val < Integer.MIN_VALUE || val > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int) val;
        }
    }
}
