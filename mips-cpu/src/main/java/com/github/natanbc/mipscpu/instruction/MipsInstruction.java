package com.github.natanbc.mipscpu.instruction;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.MipsException;
import com.github.natanbc.mipscpu.MipsRegisters;
import com.github.natanbc.mipscpu.memory.MemoryOperationException;

import static com.github.natanbc.mipscpu.MipsRegisters.*;
import static com.github.natanbc.mipscpu.instruction.TrapException.Cause.*;

public class MipsInstruction {
    public static void execute(MipsCPU cpu, int instruction) throws MipsException {
        switch (instruction >>> 26) {
            case 0: Opcode0.execute(cpu, instruction); break;
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
            case 0: return Opcode0.toString(instruction);
            case 2: return "j " + ((instruction << 6) >>> 6);
            case 3: return "jal " + ((instruction << 6) >>> 6);
            default: return toStringI(instruction);
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
                int a = cpu.registers().readInteger(rs);
                int b = signExtend(imm);
                int res = a + b;
                //overflow check
                if(((a ^ res) & (b ^ res)) < 0) {
                    throw new TrapException(Ov);
                }
                cpu.registers().writeInteger(rt, res);
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
                    default: throw new TrapException(RI, "Unknown branch type " + Integer.toBinaryString(rt));
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
                try {
                    //cast to byte to sign-extend
                    cpu.registers().writeInteger(rt, (byte)cpu.readByte(address));
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                return;
            }
            //lbu
            case 0b100100: {
                int address = cpu.registers().readInteger(rs) + signExtend(imm);
                try {
                    cpu.registers().writeInteger(rt, cpu.readByte(address) & 0xFF);
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                return;
            }
            //ldc1
            case 0b110101: {
                int address = cpu.registers().readInteger(rs) + signExtend(imm);
                if((address & ~0b111) != address) {
                    //TODO `throw cpu.addressError(address)`?
                    cpu.registers().writeInteger(C0_VADDR, address);
                    throw new TrapException(AdEL, "Unaligned memory read to 0x" + Integer.toHexString(address));
                }
                rt &= ~1;
                try {
                    cpu.registers().writeFloat(rt, cpu.readWord(address));
                    cpu.registers().writeFloat(rt + 1, cpu.readWord(address + 4));
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                return;
            }
            //lh
            case 0b100001: {
                int address = cpu.registers().readInteger(rs) + signExtend(imm);
                if((address & ~0b1) != address) {
                    cpu.registers().writeInteger(C0_VADDR, address);
                    throw new TrapException(AdEL, "Unaligned memory read to 0x" + Integer.toHexString(address));
                }
                try {
                    //cast to short to sign-extend
                    cpu.registers().writeInteger(rt, (short)cpu.readHalfWord(address));
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                return;
            }
            //lhu
            case 0b100101: {
                int address = cpu.registers().readInteger(rs) + signExtend(imm);
                if((address & ~0b1) != address) {
                    cpu.registers().writeInteger(C0_VADDR, address);
                    throw new TrapException(AdEL, "Unaligned memory read to 0x" + Integer.toHexString(address));
                }
                try {
                    cpu.registers().writeInteger(rt, cpu.readHalfWord(address) & 0xFFFF);
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                return;
            }
            //ll
            case 0b110000: {
                int address = cpu.registers().readInteger(rs) + signExtend(imm);
                if((address & ~0b11) != address) {
                    cpu.registers().writeInteger(C0_VADDR, address);
                    throw new TrapException(AdEL, "Unaligned memory read to 0x" + Integer.toHexString(address));
                }
                try {
                    cpu.registers().writeInteger(rt, cpu.readWord(address));
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                cpu.registers().startAtomicUpdate(address);
                return;
            }
            //lui
            case 0b001111: {
                cpu.registers().writeInteger(rt, imm << 16);
                return;
            }
            //lw
            case 0b100011: {
                int address = cpu.registers().readInteger(rs) + signExtend(imm);
                if((address & ~0b11) != address) {
                    cpu.registers().writeInteger(C0_VADDR, address);
                    throw new TrapException(AdEL, "Unaligned memory read to 0x" + Integer.toHexString(address));
                }
                try {
                    cpu.registers().writeInteger(rt, cpu.readWord(address));
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                return;
            }
            //lwc1
            case 0b110001: {
                int address = cpu.registers().readInteger(rs) + signExtend(imm);
                if((address & ~0b11) != address) {
                    cpu.registers().writeInteger(C0_VADDR, address);
                    throw new TrapException(AdEL, "Unaligned memory read to 0x" + Integer.toHexString(address));
                }
                try {
                    cpu.registers().writeFloat(rt, cpu.readWord(address));
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                return;
            }
            //lwl
            case 0b100010: {
                int addr = cpu.registers().readInteger(rs) + signExtend(imm);
                int word;
                try {
                    word = cpu.readWord(addr & ~0b11);
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
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
                int word;
                try {
                    word = cpu.readWord(addr & ~0b11);
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
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
                try {
                    cpu.writeByte(cpu.registers().readInteger(rs) + signExtend(imm),
                            0xFF & cpu.registers().readInteger(rt));
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                return;
            }
            //sc
            case 0b111000: {
                int address = cpu.registers().readInteger(rs) + signExtend(imm);
                if((address & ~0b11) != address) {
                    cpu.registers().writeInteger(C0_VADDR, address);
                    throw new TrapException(AdES, "Unaligned memory write to 0x" + Integer.toHexString(address));
                }
                if(cpu.registers().isAtomicUpdateValid(address)) {
                    //the write also stops the atomic update
                    try {
                        cpu.writeWord(address, rt);
                    } catch (MemoryOperationException e) {
                        throw new TrapException(DBE);
                    }
                    cpu.registers().writeInteger(rt, 1);
                } else {
                    cpu.registers().writeInteger(rt, 0);
                }
                cpu.registers().endAtomicUpdate();
                return;
            }
            //sdc1
            case 0b111101: {
                int address = cpu.registers().readInteger(rs) + signExtend(imm);
                if((address & ~0b111) != address) {
                    cpu.registers().writeInteger(C0_VADDR, address);
                    throw new TrapException(AdES, "Unaligned memory write to 0x" + Integer.toHexString(address));
                }
                rt &= ~1;
                try {
                    cpu.writeWord(address, cpu.registers().readFloat(rt));
                    cpu.writeWord(address + 4, cpu.registers().readFloat(rt + 1));
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                return;
            }
            //sh
            case 0b101001: {
                int address = cpu.registers().readInteger(rs) + signExtend(imm);
                if((address & ~0b1) != address) {
                    cpu.registers().writeInteger(C0_VADDR, address);
                    throw new TrapException(AdES, "Unaligned memory write to 0x" + Integer.toHexString(address));
                }
                try {
                    cpu.writeHalfWord(address,
                            0xFFFF & cpu.registers().readInteger(rt));
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
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
                int address = cpu.registers().readInteger(rs) + signExtend(imm);
                if((address & ~0b11) != address) {
                    cpu.registers().writeInteger(C0_VADDR, address);
                    throw new TrapException(AdES, "Unaligned memory write to 0x" + Integer.toHexString(address));
                }
                try {
                    cpu.writeWord(address, cpu.registers().readInteger(rt));
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                return;
            }
            //swc1
            case 0b111001: {
                int address = cpu.registers().readInteger(rs) + signExtend(imm);
                if((address & ~0b11) != address) {
                    cpu.registers().writeInteger(C0_VADDR, address);
                    throw new TrapException(AdES, "Unaligned memory write to 0x" + Integer.toHexString(address));
                }
                try {
                    cpu.writeWord(address, cpu.registers().readFloat(rt));
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                return;
            }
            //swl
            case 0b101010: {
                int addr = cpu.registers().readInteger(rs) + signExtend(imm);
                int word;
                try {
                    word = cpu.readWord(addr & ~0b11);
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                int register = cpu.registers().readInteger(rt);
                switch (addr & 0b11) {
                    case 0: word = register; break;
                    case 1: word = (word & 0xFF000000) | (register >>> 24); break;
                    case 2: word = (word & 0xFFFF0000) | (register >>> 16); break;
                    case 3: word = (word & 0xFFFFFF00) | (register >>>  8); break;
                }
                try {
                    cpu.writeWord(addr & ~0b11, word);
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                return;
            }
            //swr
            case 0b101110: {
                int addr = cpu.registers().readInteger(rs) + signExtend(imm);
                int word;
                try {
                    word = cpu.readWord(addr & ~0b11);
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                int register = cpu.registers().readInteger(rt);
                switch (addr & 0b11) {
                    case 0: word = ((register & 0x0000FF) << 24) | (word & 0xFFFFFF); break;
                    case 1: word = ((register & 0x00FFFF) << 16) | (word & 0x00FFFF); break;
                    case 2: word = ((register & 0xFFFFFF) <<  8) | (word & 0x0000FF); break;
                    case 3: word = register; break;
                }
                try {
                    cpu.writeWord(addr & ~0b11, word);
                } catch (MemoryOperationException e) {
                    throw new TrapException(DBE);
                }
                return;
            }
            //xori
            case 0b001110: {
                cpu.registers().writeInteger(rt, cpu.registers().readInteger(rs) ^ imm);
                return;
            }
            //COP0
            case 0b010000: {
                COP0.execute(cpu, instruction);
                return;
            }
            //COP1
            case 0b010001: {
                COP1.execute(cpu, instruction);
                return;
            }
            default: throw new TrapException(RI, "Unknown opcode " + Integer.toBinaryString(instruction >>> 26));
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
            case 0b110101: return "ldc1 " + fr(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b100001: return "lh " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b100101: return "lhu " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b110000: return "ll " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b001111: return "lui " + ir(rt) + ", " + imm;
            case 0b100011: return "lw " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b110001: return "lwc1 " + fr(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b100010: return "lwl " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b100110: return "lwr " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b001101: return "ori " + iregs(rt, rs) + ", " + imm;
            case 0b101000: return "sb " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b111000: return "sc " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b111101: return "sdc1 " + fr(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b101001: return "sh " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b001010: return "slti " + iregs(rt, rs) + ", " + signExtend(imm);
            case 0b001011: return "sltiu " + iregs(rt, rs) + ", " + signExtend(imm);
            case 0b101011: return "sw " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b111001: return "swc1 " + fr(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b101010: return "swl " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b101110: return "swr " + ir(rt) + ", " + signExtend(imm) + "(" + ir(rs) + ")";
            case 0b001110: return "xori " + iregs(rt, rs) + ", " + imm;
            case 0b010000: return COP0.toString(instruction);
            case 0b010001: return COP1.toString(instruction);
            default: return invalid(instruction, "Unknown opcode " + Integer.toBinaryString(instruction >>> 26));
        }
    }

    static String invalid(int instruction, String reason) {
        return "Invalid instruction " + Integer.toHexString(instruction) + ": " + reason;
    }

    static String iregs(int a, int b, int c) {
        return String.join(", ", ir(a), ir(b), ir(c));
    }

    static String iregs(int a, int b) {
        return String.join(", ", ir(a), ir(b));
    }

    //*i*nteger *r*egister
    static String ir(int r) { return MipsRegisters.integerName(r); }

    static String fregs(int a, int b, int c) {
        return String.join(", ", fr(a), fr(b), fr(c));
    }

    static String fregs(int a, int b) {
        return String.join(", ", fr(a), fr(b));
    }

    //*f*loat *r*egister
    static String fr(int r) { return MipsRegisters.floatName(r); }

    static int signExtend(int imm) {
        return (short)imm;
    }
}
