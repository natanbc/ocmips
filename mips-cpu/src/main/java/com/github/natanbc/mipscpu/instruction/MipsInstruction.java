package com.github.natanbc.mipscpu.instruction;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.MipsException;
import com.github.natanbc.mipscpu.MipsRegisters;

import static com.github.natanbc.mipscpu.MipsRegisters.*;

public class MipsInstruction {
    public static void execute(MipsCPU cpu, int instruction) throws MipsException {
        switch (instruction >>> 26) {
            case 0: executeOpcode0(cpu, instruction); break;
            // j <imm26>
            case 2: {
                int offset = (instruction << 6) >>> 6;
                int pc = cpu.registers().readInteger(PC);
                //PC = nPC;
                pc += 4;
                //nPC = (PC & 0xf0000000) | (target << 2)
                cpu.registers().writeInteger(PC, (pc & 0xf0000000) | (offset << 2));
                break;
            }
            // jal <imm26>
            case 3: {
                int offset = (instruction << 6) >>> 6;
                int pc = cpu.registers().readInteger(PC);
                cpu.registers().writeInteger(RA, pc + 8);
                //PC = nPC;
                pc += 4;
                //nPC = (PC & 0xf0000000) | (target << 2)
                cpu.registers().writeInteger(PC, (pc & 0xf0000000) | (offset << 2));
                break;
            }
            default: executeI(cpu, instruction);
        }
    }

    public static String toString(int instruction) {
        switch (instruction >>> 26) {
            case 0: return toStringOpcode0(instruction);
            case 2: return "j " + ((instruction << 6) >>> 6);
            case 3: return "jal " + ((instruction << 6) >>> 6);
            default: return toStringI(instruction);
        }
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    private static void executeOpcode0(MipsCPU cpu, int instruction) throws MipsException {
        int func =   instruction & 0b111111;
        int rd =    (instruction << 16) >>> 27;
        int rs =    (instruction <<  6) >>> 27;
        int rt =    (instruction << 11) >>> 27;
        int shamt = (instruction << 21) >>> 27;
        switch (func) {
            //add
            case 0b100000:  {
                cpu.registers().writeInteger(rd,
                        cpu.registers().readInteger(rs) + cpu.registers().readInteger(rt));
                //TODO overflow check
                return;
            }
            //addu
            case 0b100001: {
                cpu.registers().writeInteger(rd,
                        cpu.registers().readInteger(rs) + cpu.registers().readInteger(rt));
                return;
            }
            //and
            case 0b100100: {
                cpu.registers().writeInteger(rd,
                        cpu.registers().readInteger(rs) & cpu.registers().readInteger(rt));
                return;
            }
            //div
            case 0b011010: {
                int a = cpu.registers().readInteger(rs);
                int b = cpu.registers().readInteger(rt);
                cpu.registers().writeInteger(LO, a / b);
                cpu.registers().writeInteger(HI, a % b);
                return;
            }
            //divu
            case 0b011011: {
                int a = cpu.registers().readInteger(rs);
                int b = cpu.registers().readInteger(rt);
                cpu.registers().writeInteger(LO, Integer.divideUnsigned(a, b));
                cpu.registers().writeInteger(HI, Integer.remainderUnsigned(a, b));
                return;
            }
            //jalr
            case 0b001001: {
                int temp = cpu.registers().readInteger(rs);
                cpu.registers().writeInteger(rd, cpu.registers().readInteger(PC) + 8);
                cpu.registers().writeInteger(PC, temp);
                return;
            }
            //jr
            case 0b001000: {
                cpu.registers().writeInteger(PC, cpu.registers().readInteger(rs));
                return;
            }
            //mfhi
            case 0b010000: {
                cpu.registers().writeInteger(rd, cpu.registers().readInteger(HI));
                return;
            }
            //mflo
            case 0b010010: {
                cpu.registers().writeInteger(rd, cpu.registers().readInteger(LO));
                return;
            }
            //mult
            case 0b011000: {
                cpu.registers().writeInteger(LO,
                        cpu.registers().readInteger(rs) * cpu.registers().readInteger(rt));
                //TODO overflow check
                return;
            }
            //multu
            case 0b011001: {
                cpu.registers().writeInteger(LO,
                        cpu.registers().readInteger(rs) * cpu.registers().readInteger(rt));
                return;
            }
            //or
            case 0b100101: {
                cpu.registers().writeInteger(rd,
                        cpu.registers().readInteger(rs) | cpu.registers().readInteger(rt));
                return;
            }
            //sll
            case 0b000000: {
                cpu.registers().writeInteger(rd, cpu.registers().readInteger(rt) << shamt);
                return;
            }
            //sllv
            case 0b000100: {
                cpu.registers().writeInteger(rd,
                        cpu.registers().readInteger(rt) << cpu.registers().readInteger(rs));
                return;
            }
            //slt
            case 0b101010: {
                if(cpu.registers().readInteger(rs) < cpu.registers().readInteger(rt)) {
                    cpu.registers().writeInteger(rd, 1);
                } else {
                    cpu.registers().writeInteger(rd, 0);
                }
                return;
            }
            //sltu
            case 0b101011: {
                int cmp = Integer.compareUnsigned(cpu.registers().readInteger(rs), cpu.registers().readInteger(rt));
                if(cmp < 0) {
                    cpu.registers().writeInteger(rd, 1);
                } else {
                    cpu.registers().writeInteger(rd, 0);
                }
                return;
            }
            //sra
            case 0b000011: {
                cpu.registers().writeInteger(rd, cpu.registers().readInteger(rt) >> shamt);
                return;
            }
            //srav
            case 0b000111: {
                cpu.registers().writeInteger(rd,
                        cpu.registers().readInteger(rt) >> cpu.registers().readInteger(rs));
                return;
            }
            //srl
            case 0b000010: {
                cpu.registers().writeInteger(rd, cpu.registers().readInteger(rt) >>> shamt);
                return;
            }
            //srlv
            case 0b000110: {
                cpu.registers().writeInteger(rd,
                        cpu.registers().readInteger(rt) >>> cpu.registers().readInteger(rs));
                return;
            }
            //sub
            case 0b100010: {
                cpu.registers().writeInteger(rd,
                        cpu.registers().readInteger(rs) - cpu.registers().readInteger(rt));
                //TODO overflow check
                return;
            }
            //subu
            case 0b100011: {
                cpu.registers().writeInteger(rd,
                        cpu.registers().readInteger(rs) - cpu.registers().readInteger(rt));
                return;
            }
            //syscall
            case 0b001100: {
                SyscallHandler h = cpu.getSyscallHandler();
                if(h == null) {
                    throw new InstructionExecutionException("No syscall handler set!");
                }
                try {
                    h.handleSyscall(cpu);
                } catch (RuntimeException e) {
                    cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + 4);
                    throw e;
                }
                return;
            }
            //teq
            case 0b110100: {
                if(cpu.registers().readInteger(rs) == cpu.registers().readInteger(rt)) {
                    throw new TrapException(instruction, 0);
                }
                return;
            }
            //xor
            case 0b100110: {
                cpu.registers().writeInteger(rd,
                        cpu.registers().readInteger(rs) ^ cpu.registers().readInteger(rt));
                return;
            }
            default: throw new IllegalInstructionException(instruction, "Unknown func " + func);
        }
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    private static void executeI(MipsCPU cpu, int instruction) throws MipsException {
        int rs =  (instruction <<  6) >>> 27;
        int rt =  (instruction << 11) >>> 27;
        int imm = instruction & 0xFFFF;
        //andi, ori, xori, lui must NOT be sign extended
        //addi[u], lw, sw, relative branches, slti[u] must be sign extended
        switch (instruction >>> 26) {
            //addi
            case 0b001000: {
                cpu.registers().writeInteger(rt, cpu.registers().readInteger(rs) + signExtend(imm));
                //TODO overflow check
                return;
            }
            //addiu
            case 0b001001: {
                cpu.registers().writeInteger(rt, cpu.registers().readInteger(rs) + signExtend(imm));
                return;
            }
            //andi
            case 0b001100: {
                cpu.registers().writeInteger(rt, cpu.registers().readInteger(rs) & imm);
                return;
            }
            //beq
            case 0b000100: {
                if(cpu.registers().readInteger(rs) == cpu.registers().readInteger(rt)) {
                    cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + 4 + (signExtend(imm) << 2));
                }
                return;
            }
            //bgez, bgezal, bltz, bltzal
            case 0b000001:
                switch(rt) {
                    //bgez
                    case 0b00001: {
                        if(cpu.registers().readInteger(rs) >= 0) {
                            cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + 4 + (signExtend(imm) << 2));
                        }
                        return;
                    }
                    //bgezal
                    case 0b10001: {
                        if(cpu.registers().readInteger(rs) >= 0) {
                            cpu.registers().writeInteger(RA, cpu.registers().readInteger(PC) + 8);
                            cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + 4 + (signExtend(imm) << 2));
                        }
                        return;
                    }
                    //bltz
                    case 0b00000: {
                        if(cpu.registers().readInteger(rs) < 0) {
                            cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + 4 + (signExtend(imm) << 2));
                        }
                        return;
                    }
                    //bltzal
                    case 0b10000: {
                        if(cpu.registers().readInteger(rs) < 0) {
                            cpu.registers().writeInteger(RA, cpu.registers().readInteger(PC) + 8);
                            cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + 4 + (signExtend(imm) << 2));
                        }
                        return;
                    }
                    default: throw new IllegalInstructionException(instruction, "Unknown branch type " + Integer.toBinaryString(rt));
                }
                //bgtz
            case 0b000111: {
                if(cpu.registers().readInteger(rs) > 0) {
                    cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + 4 + (signExtend(imm) << 2));
                }
                return;
            }
            //blez
            case 0b000110: {
                if(cpu.registers().readInteger(rs) <= 0) {
                    cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + 4 + (signExtend(imm) << 2));
                }
                return;
            }
            //bne
            case 0b000101: {
                if(cpu.registers().readInteger(rs) != cpu.registers().readInteger(rt)) {
                    cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + 4 + (signExtend(imm) << 2));
                }
                return;
            }
            //lb
            case 0b100000: {
                int address = cpu.registers().readInteger(rs) + signExtend(imm);
                //cast to byte to sign-extend
                cpu.registers().writeInteger(rt, (byte)cpu.readByte(address));
                return;
            }
            //lbu
            case 0b100100: {
                int address = cpu.registers().readInteger(rs) + signExtend(imm);
                cpu.registers().writeInteger(rt, cpu.readByte(address) & 0xFF);
                return;
            }
            //lui
            case 0b001111: {
                cpu.registers().writeInteger(rt, imm << 16);
                return;
            }
            //lw
            case 0b100011: {
                int addr = cpu.registers().readInteger(rs) + signExtend(imm);
                if((addr & ~0b11) != addr) {
                    throw new InstructionExecutionException("Unaligned memory read to 0x" + Integer.toHexString(addr));
                }
                cpu.registers().writeInteger(rt, cpu.readWord(addr));
                return;
            }
            //lwc1
            case 0b110001: {
                int addr = cpu.registers().readInteger(rs) + signExtend(imm);
                if((addr & ~0b11) != addr) {
                    throw new InstructionExecutionException("Unaligned memory read to 0x" + Integer.toHexString(addr));
                }
                cpu.registers().writeFloat(rt, cpu.readWord(addr));
                return;
            }
            //lwl
            case 0b100010: {
                int addr = cpu.registers().readInteger(rs) + signExtend(imm);
                int word = cpu.readWord(addr & ~0b11);
                int register = cpu.registers().readInteger(rt);
                switch (addr & 0b11) {
                    case 0: register = word; break;
                    case 1: register = ((word<< 8) & ~0x0000FF) | (register & 0x0000FF); break;
                    case 2: register = ((word<<16) & ~0x00FFFF) | (register & 0x00FFFF); break;
                    case 3: register = ((word<<24) & ~0xFFFFFF) | (register & 0xFFFFFF); break;
                }
                cpu.registers().writeInteger(rt, register);
                return;
            }
            //lwr
            case 0b100110: {
                int addr = cpu.registers().readInteger(rs) + signExtend(imm);
                int word = cpu.readWord(addr & ~0b11);
                int register = cpu.registers().readInteger(rt);
                switch (addr & 0b11) {
                    case 0: register = ((word>>>24) & 0x0000FF) | (register & ~0x0000FF); break;
                    case 1: register = ((word>>>16) & 0x00FFFF) | (register & ~0x00FFFF); break;
                    case 2: register = ((word>>> 8) & 0xFFFFFF) | (register & ~0xFFFFFF); break;
                    case 3: register = word; break;
                }
                cpu.registers().writeInteger(rt, register);
                return;
            }
            //ori
            case 0b001101: {
                cpu.registers().writeInteger(rt, cpu.registers().readInteger(rs) | imm);
                return;
            }
            //sb
            case 0b101000: {
                cpu.writeByte(cpu.registers().readInteger(rs) + signExtend(imm),
                        0xFF & cpu.registers().readInteger(rt));
                return;
            }
            //slti
            case 0b001010: {
                if(cpu.registers().readInteger(rs) < signExtend(imm)) {
                    cpu.registers().writeInteger(rt, 1);
                } else {
                    cpu.registers().writeInteger(rt, 0);
                }
                return;
            }
            //sltiu
            case 0b001011: {
                if(cpu.registers().readInteger(rs) < signExtend(imm)) {
                    cpu.registers().writeInteger(rt, 1);
                } else {
                    cpu.registers().writeInteger(rt, 0);
                }
                return;
            }
            //sw
            case 0b101011: {
                int addr = cpu.registers().readInteger(rs) + signExtend(imm);
                if((addr & ~0b11) != addr) {
                    throw new InstructionExecutionException("Unaligned memory write to 0x" + Integer.toHexString(addr));
                }
                cpu.writeWord(addr, cpu.registers().readInteger(rt));
                return;
            }
            //swc1
            case 0b111001: {
                int addr = cpu.registers().readInteger(rs) + signExtend(imm);
                if((addr & ~0b11) != addr) {
                    throw new InstructionExecutionException("Unaligned memory write to 0x" + Integer.toHexString(addr));
                }
                cpu.writeWord(addr, cpu.registers().readFloat(rt));
                return;
            }
            //swl
            case 0b101010: {
                int addr = cpu.registers().readInteger(rs) + signExtend(imm);
                int word = cpu.readWord(addr & ~0b11);
                int register = cpu.registers().readInteger(rt);
                switch (addr & 0b11) {
                    case 0: word = register; break;
                    case 1: word = (word & 0xFF000000) | (register >>> 24); break;
                    case 2: word = (word & 0xFFFF0000) | (register >>> 16); break;
                    case 3: word = (word & 0xFFFFFF00) | (register >>>  8); break;
                }
                cpu.writeWord(addr & ~0b11, word);
                return;
            }
            //swr
            case 0b101110: {
                int addr = cpu.registers().readInteger(rs) + signExtend(imm);
                int word = cpu.readWord(addr & ~0b11);
                int register = cpu.registers().readInteger(rt);
                switch (addr & 0b11) {
                    case 0: word = ((register & 0x0000FF) << 24) | (word & 0xFFFFFF); break;
                    case 1: word = ((register & 0x00FFFF) << 16) | (word & 0x00FFFF); break;
                    case 2: word = ((register & 0xFFFFFF) <<  8) | (word & 0x0000FF); break;
                    case 3: word = register; break;
                }
                cpu.writeWord(addr & ~0b11, word);
                return;
            }
            //xori
            case 0b001110: {
                cpu.registers().writeInteger(rt, cpu.registers().readInteger(rs) ^ imm);
                return;
            }
            //COP1
            case 0b010001: {
                executeCOP1(cpu, instruction);
                return;
            }
            default: throw new IllegalInstructionException(instruction, "Unknown opcode " + Integer.toBinaryString(instruction >>> 26));
        }
    }

    //i fear no man
    //but that thing
    //it scares me
    private static void executeCOP1(MipsCPU cpu, int instruction) throws MipsException {
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
                    float vd = 0.0f;
                    switch (otyp1) {
                        // ADD
                        case 0: vd = vs1 + vs2; break;
                        // SUB
                        case 1: vd = vs1 - vs2; break;
                        // MUL
                        case 2: vd = vs1 * vs2; break;
                        // DIV
                        case 3: vd = vs1 / vs2; break;
                        // ABS
                        case 5:  vd = Math.abs(vs1); break;
                        // MOV
                        case 6: vd = vs1; break;
                        // NEG
                        case 7: vd = -vs1; break;
                        // CVT from S to D
                        case 33: {
                            long vdi = Double.doubleToLongBits(vs1);
                            fd &= ~1;
                            cpu.registers().writeFloat(fd, (int)(vdi));
                            cpu.registers().writeFloat(fd + 1, (int)(vdi>>>32));
                            fd = -1;
                            break;
                        }
                        // CVT from S to W
                        case 36: {
                            cpu.registers().writeFloat(fd, (int)vs1);
                            fd = -1;
                            break;
                        }
                        default: throw new IllegalInstructionException(instruction, "Unimplemented SP COP1");
                    }
                    if(fd != -1) {
                        cpu.registers().writeFloat(fd, Float.floatToIntBits(vd));
                    }
                    break;
                }
                //DP
                case 17: {
                    fs &= ~1; ft &= ~1;
                    double vs1 = Double.longBitsToDouble(
                            (((long)cpu.registers().readFloat(fs))&0xFFFFFFFFL)
                                    |((((long)cpu.registers().readFloat(fs+1))&0xFFFFFFFFL)<<32L)
                    );
                    double vs2 = Double.longBitsToDouble(
                            (((long)cpu.registers().readFloat(ft))&0xFFFFFFFFL)
                                    |((((long)cpu.registers().readFloat(ft+1))&0xFFFFFFFFL)<<32L)
                    );
                    double vd = 0;
                    switch (otyp1) {
                        // ADD
                        case 0: vd = vs1 + vs2; break;
                        // SUB
                        case 1: vd = vs1 - vs2; break;
                        // MUL
                        case 2: vd = vs1 * vs2; break;
                        // DIV
                        case 3: vd = vs1 / vs2; break;
                        // ABS
                        case 5: vd = Math.abs(vs1); break;
                        // MOV
                        case 6: vd = vs1; break;
                        // NEG
                        case 7: vd = -vs1; break;
                        // CVT from D to S
                        case 32: {
                            cpu.registers().writeFloat(fd, Float.floatToIntBits((float)vs1));
                            fd = -1;
                            break;
                        }
                        // CVT from D to W
                        case 36: {
                            cpu.registers().writeFloat(fd, (int)vs1);
                            fd = -1;
                            break;
                        }
                        default: throw new IllegalInstructionException(instruction, "Unimplemented DP COP1");
                    }
                    if (fd != -1) {
                        long vdi = Double.doubleToLongBits(vd);
                        fd &= ~1;
                        cpu.registers().writeFloat(fd, (int)(vdi));
                        cpu.registers().writeFloat(fd+1, (int)(vdi>>>32));
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
                        default: throw new IllegalInstructionException(instruction, "Unimplemented W COP1");
                    }
                    break;
                }
                default: throw new IllegalInstructionException(instruction, "Unknown COP1 size");
            }
        } else {
            switch (rs) {
                // MFCn
                case 0: {
                    cpu.registers().writeInteger(rt, cpu.registers().readFloat(rd));
                    break;
                }
                // CFCn
                case 2: {
                    int tmp;
                    // FCR0: Revision
                    if (rd == 0) {
                        tmp = 0x0300;
                    } else {
                        throw new IllegalInstructionException(instruction, "Unimplemented COP1 CFCn");
                    }
                    cpu.registers().writeInteger(rt, tmp);
                    break;
                }
                // MTCn
                case 4: {
                    cpu.registers().writeFloat(fs, cpu.registers().readInteger(rt));
                    break;
                }
                // CTCn
                case 6: {
                    throw new IllegalInstructionException(instruction, "Unimplemented COP1 CTCn");
                }
                default: {
                    throw new IllegalInstructionException(instruction, "Unimplemented COP1");
                }
            }
        }
    }

    private static String toStringOpcode0(int instruction) {
        int func =   instruction & 0b111111;
        int rd =    (instruction << 16) >>> 27;
        int rs =    (instruction <<  6) >>> 27;
        int rt =    (instruction << 11) >>> 27;
        int shamt = (instruction << 21) >>> 27;
        switch (func) {
            case 0b100000: return "add " + iregs(rd, rs, rt);
            case 0b100001: return "addu " + iregs(rd, rs, rt);
            case 0b100100: return "and " + iregs(rd, rs, rt);
            case 0b011010: return "div " + iregs(rs, rt);
            case 0b011011: return "divu " + iregs(rs, rt);
            case 0b001001: return "jalr " + iregs(rs, rd);
            case 0b001000: return "jr " + ir(rs);
            case 0b010000: return "mfhi " + ir(rd);
            case 0b010010: return "mflo " + ir(rd);
            case 0b011000: return "mult " + iregs(rs, rt);
            case 0b011001: return "multu " + iregs(rs, rt);
            case 0b100101: return "or " + iregs(rd, rs, rt);
            case 0b000000: return "sll " + iregs(rd, rt) + ", " + shamt;
            case 0b000100: return "sllv " + iregs(rd, rs, rt);
            case 0b101010: return "slt " + iregs(rd, rs, rt);
            case 0b101011: return "sltu " + iregs(rd, rs, rt);
            case 0b000011: return "sra " + iregs(rd, rt) + ", " + shamt;
            case 0b000111: return "srav " + iregs(rd, rt, rs);
            case 0b000010: return "srl " + iregs(rd, rt) + ", " + shamt;
            case 0b000110: return "srlv " + iregs(rd, rt, rs);
            case 0b100010: return "sub " + iregs(rd, rs, rt);
            case 0b100011: return "subu " + iregs(rd, rs, rt);
            case 0b001100: return "syscall";
            case 0b110100: return "teq " + iregs(rs, rt);
            case 0b100110: return "xor " + iregs(rd, rs, rt);
            default: return invalid(instruction, "Unknown func " + func);
        }
    }

    private static String toStringI(int instruction) {
        int rs =  (instruction <<  6) >>> 27;
        int rt =  (instruction << 11) >>> 27;
        int imm = instruction & 0xFFFF;
        //andi, ori, xori must NOT be sign extended
        //addi[u], lw, sw, relative branches, slti[u] must be sign extended
        switch (instruction >>> 26) {
            case 0b001000: return "addi " + iregs(rt, rs) + ", " + signExtend(imm);
            case 0b001001: return "addiu " + iregs(rt, rs) + ", " + signExtend(imm);
            case 0b001100: return "andi " + iregs(rt, rs) + ", " + imm;
            case 0b000100: return "beq " + iregs(rs, rt) + ", " + signExtend(imm);
            case 0b000001:
                switch (rt) {
                    case 0b00001: return "bgez " + ir(rs) + ", " + signExtend(imm);
                    case 0b10001: return "bgezal " + ir(rs) + ", " + signExtend(imm);
                    case 0b00000: return "bltz " + ir(rs) + ", " + signExtend(imm);
                    case 0b10000: return "bltzal " + ir(rs) + ", " + signExtend(imm);
                    default: return invalid(instruction, "Unknown branch type " + Integer.toBinaryString(rt));
                }
            case 0b000111: return "bgtz " + ir(rs) + ", " + signExtend(imm);
            case 0b000110: return "blez " + ir(rs) + ", " + signExtend(imm);
            case 0b000101: return "bne " + iregs(rs, rt) + ", " + signExtend(imm);
            case 0b100000: return "lb " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b100100: return "lbu " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b001111: return "lui " + ir(rt) + ", " + imm;
            case 0b100011: return "lw " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b110001: return "lwc1 " + fr(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b100010: return "lwl " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b100110: return "lwr " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b001101: return "ori " + iregs(rt, rs) + ", " + imm;
            case 0b101000: return "sb " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b001010: return "slti " + iregs(rt, rs) + ", " + signExtend(imm);
            case 0b001011: return "sltiu " + iregs(rt, rs) + ", " + signExtend(imm);
            case 0b101011: return "sw " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b111001: return "swc1 " + fr(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b101010: return "swl " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b101110: return "swr " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b001110: return "xori " + iregs(rt, rs) + ", " + imm;
            case 0b010001: return toStringCOP1(instruction);
            default: return invalid(instruction, "Unknown opcode " + Integer.toBinaryString(instruction >>> 26));
        }
    }

    private static String toStringCOP1(int instruction) {
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
                case 16: {
                    switch (otyp1) {
                        case 0: return "add.s " + fregs(fd, fs, ft);
                        case 1: return "sub.s " + fregs(fd, fs, ft);
                        case 2: return "mul.s " + fregs(fd, fs, ft);
                        case 3: return "div.s " + fregs(fd, fs, ft);
                        case 5: return "abs.s " + fregs(fd, fs);
                        case 6: return "mov.s " + fregs(fd, fs);
                        case 7: return "neg.s " + fregs(fd, fs);
                        case 33: return "cvt.d.s " + fregs(fd & ~1, fs);
                        case 36: return "cvt.w.s " + fregs(fd, fs);
                        default: return invalid(instruction, "Unimplemented SP COP1");
                    }
                }
                //DP
                case 17: {
                    switch (otyp1) {
                        case 0: return "add.d " + fregs(fd, fs, ft);
                        case 1: return "sub.d " + fregs(fd, fs, ft);
                        case 2: return "mul.d " + fregs(fd, fs, ft);
                        case 3: return "div.d " + fregs(fd, fs, ft);
                        case 5: return "abs.d " + fregs(fd, fs);
                        case 6: return "mov.d " + fregs(fd, fs);
                        case 7: return "neg.d " + fregs(fd, fs);
                        case 32: return "cvt.s.d " + fregs(fd, fs);
                        case 36: return "cvt.w.d " + fregs(fd, fs);
                        default: return invalid(instruction, "Unimplemented DP COP1");
                    }
                }
                // W
                case 20: {
                    switch (otyp1) {
                        case 32: return "cvt.s.w " + fregs(fd, fs);
                        case 33: return "cvt.d.w " + fregs(fd, fs);
                        default: return invalid(instruction, "Unimplemented W COP1");
                    }
                }
                default: return invalid(instruction, "Unknown COP1 size");
            }
        } else {
            switch (rs) {
                // MFCn
                case 0: return "mfc1 " + ir(rt) + ", " + fr(fs);
                // CFCn
                case 2: {
                    // FCR0: Revision
                    if (rd == 0) {
                        return "cfc1 " + ir(rt) + ", FIR";
                    } else {
                        return invalid(instruction, "Unimplemented COP1 CFCn");
                    }
                }
                // MTCn
                case 4: return "mtc1 " + fr(fs) + ", " + ir(rt);
                // CTCn
                case 6: {
                    return invalid(instruction, "Unimplemented COP1 CTCn");
                }
                default: {
                    return invalid(instruction, "Unimplemented COP1");
                }
            }
        }
    }

    private static String invalid(int instruction, String reason) {
        return "Invalid instruction " + Integer.toHexString(instruction) + ": " + reason;
    }

    private static String iregs(int a, int b, int c) {
        return String.join(", ", ir(a), ir(b), ir(c));
    }

    private static String iregs(int a, int b) {
        return String.join(", ", ir(a), ir(b));
    }

    //*i*nteger *r*egister
    private static String ir(int r) { return MipsRegisters.integerName(r); }

    private static String fregs(int a, int b, int c) {
        return String.join(", ", fr(a), fr(b), fr(c));
    }

    private static String fregs(int a, int b) {
        return String.join(", ", fr(a), fr(b));
    }

    //*f*loat *r*egister
    private static String fr(int r) { return MipsRegisters.floatName(r); }

    private static int signExtend(int imm) {
        return (short)imm;
    }
}
