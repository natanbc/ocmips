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
            default: throw new IllegalInstructionException(instruction, "Unknown opcode " + Integer.toBinaryString(instruction >>> 26));
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
            case 0b001101: return "ori " + iregs(rt, rs) + ", " + imm;
            case 0b101000: return "sb " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b001010: return "slti " + iregs(rt, rs) + ", " + signExtend(imm);
            case 0b001011: return "sltiu " + iregs(rt, rs) + ", " + signExtend(imm);
            case 0b101011: return "sw " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b001110: return "xori " + iregs(rt, rs) + ", " + imm;
            default: return invalid(instruction, "Unknown opcode " + Integer.toBinaryString(instruction >>> 26));
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

    private static int signExtend(int imm) {
        return (short)imm;
    }
}
