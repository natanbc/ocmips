package com.github.natanbc.mipscpu.instruction;

import static com.github.natanbc.mipscpu.instruction.MipsInstruction.*;

class MipsDecoder {
    static MipsInstruction decode(int instruction) {
        switch (instruction >>> 26) {
            case 0: return decodeOpcode0(instruction);
            case 2: return new J((instruction << 6) >> 6);
            case 3: return new Jal((instruction << 6) >> 6);
            default: return decodeI(instruction);
        }
    }

    private static MipsInstruction decodeOpcode0(int instruction) {
        int func =   instruction & 0b111111;
        int rd =    (instruction << 16) >>> 27;
        int rs =    (instruction <<  6) >>> 27;
        int rt =    (instruction << 11) >>> 27;
        int shamt = (instruction << 21) >>> 27;
        switch (func) {
            case 0b100000: return new Add(rd, rs, rt);
            case 0b100001: return new Addu(rd, rs, rt);
            case 0b100100: return new And(rd, rs, rt);
            case 0b011010: return new Div(rs, rt);
            case 0b011011: return new Divu(rs, rt);
            case 0b001001: return new Jalr(rs, rd);
            case 0b001000: return new Jr(rs);
            case 0b010000: return new Mfhi(rd);
            case 0b010010: return new Mflo(rd);
            case 0b011000: return new Mult(rs, rt);
            case 0b011001: return new Multu(rs, rt);
            case 0b100101: return new Or(rd, rs, rt);
            case 0b000000: return new Sll(rd, rt, shamt);
            case 0b000100: return new Sllv(rd, rt, rs);
            case 0b101010: return new Slt(rd, rs, rt);
            case 0b101011: return new Sltu(rd, rs, rt);
            case 0b000011: return new Sra(rd, rt, shamt);
            case 0b000111: return new Srav(rd, rt, rs);
            case 0b000010: return new Srl(rd, rt, shamt);
            case 0b000110: return new Srlv(rd, rt, rs); /* order is rt, rs */
            case 0b100010: return new Sub(rd, rs, rt);
            case 0b100011: return new Subu(rd, rs, rt);
            case 0b001100: return new Syscall();
            case 0b100110: return new Xor(rd, rs, rt);
            default: return new Invalid(instruction, "Unknown func " + func);
        }
    }

    private static MipsInstruction decodeI(int instruction) {
        int rs =  (instruction <<  6) >>> 27;
        int rt =  (instruction << 11) >>> 27;
        int imm = instruction & 0xFFFF;
        //andi, ori, xori must NOT be sign extended
        //addi[u], lw, sw, relative branches, slti[u] must be sign extended
        switch (instruction >>> 26) {
            case 0b001000: return new Addi(rt, rs, signExtend(imm));
            case 0b001001: return new Addiu(rt, rs, signExtend(imm));
            case 0b001100: return new Andi(rt, rs, imm);
            case 0b000100: return new Beq(rs, rt, signExtend(imm));
            case 0b000001:
                switch(rt) {
                    case 0b00001: return new Bgez(rs, signExtend(imm));
                    case 0b10001: return new Bgezal(rs, signExtend(imm));
                    case 0b00000: return new Bltz(rs, signExtend(imm));
                    case 0b10000: return new Bltzal(rs, signExtend(imm));
                    default: return new Invalid(instruction, "Unknown branch type " + Integer.toBinaryString(rt));
                }
            case 0b000111: return new Bgtz(rs, signExtend(imm));
            case 0b000110: return new Blez(rs, signExtend(imm));
            case 0b000101: return new Bne(rs, rt, signExtend(imm));
            case 0b100000: return new Lb(rt, signExtend(imm), rs);
            case 0b100100: return new Lbu(rt, signExtend(imm), rs);
            case 0b001111: return new Lui(rt, imm);
            case 0b100011: return new Lw(rt, signExtend(imm), rs);
            case 0b001101: return new Ori(rt, rs, imm);
            case 0b101000: return new Sb(rt, signExtend(imm), rs);
            case 0b001010: return new Slti(rt, rs, signExtend(imm));
            case 0b001011: return new Sltiu(rt, rs, signExtend(imm));
            case 0b101011: return new Sw(rt, signExtend(imm), rs);
            case 0b001110: return new Xori(rt, rs, imm);
            default: return new Invalid(instruction, "Unknown opcode " + Integer.toBinaryString(instruction >>> 26));
        }
    }
    
    private static int signExtend(int imm) {
        return (short)imm;
    }
}
