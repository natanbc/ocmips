package com.github.natanbc.mipscpu.instruction;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.MipsException;

import static com.github.natanbc.mipscpu.MipsRegisters.*;
import static com.github.natanbc.mipscpu.instruction.MipsInstruction.*;
import static com.github.natanbc.mipscpu.instruction.TrapException.Cause.*;

class Opcode0 {
    static void execute(MipsCPU cpu, int instruction) throws MipsException {
        int func =   instruction & 0b111111;
        int rd =    (instruction << 16) >>> 27;
        int rs =    (instruction <<  6) >>> 27;
        int rt =    (instruction << 11) >>> 27;
        int shamt = (instruction << 21) >>> 27;
        switch (func) {
            //add
            case 0b100000:  {
                int a = cpu.registers().readInteger(rs);
                int b = cpu.registers().readInteger(rt);
                int res = a + b;
                //overflow check
                if(((a ^ res) & (b ^ res)) < 0) {
                    throw new TrapException(Ov);
                }
                cpu.registers().writeInteger(rd, res);
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
            //break
            case 0b001101: {
                throw new TrapException(Bp);
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
            //mthi
            case 0b010001: {
                cpu.registers().writeInteger(HI, cpu.registers().readInteger(rs));
                return;
            }
            //mtlo
            case 0b010011: {
                cpu.registers().writeInteger(LO, cpu.registers().readInteger(rs));
                return;
            }
            //mult
            case 0b011000: {
                int a = cpu.registers().readInteger(rs);
                int b = cpu.registers().readInteger(rt);
                long res = (long)a * (long)b;

                cpu.registers().writeInteger(LO, (int)res);
                cpu.registers().writeInteger(HI, (int)(res >>> 32));
                return;
            }
            //multu
            case 0b011001: {
                int a = cpu.registers().readInteger(rs);
                int b = cpu.registers().readInteger(rt);

                long res = ((long)a & 0xFFFFFFFFL) * ((long)b & 0xFFFFFFFFL);

                cpu.registers().writeInteger(LO, (int)(res >> 32));
                cpu.registers().writeInteger(HI, (int)((res << 32) >> 32));
                return;
            }
            //nor
            case 0b100111: {
                int s = cpu.registers().readInteger(rs);
                int t = cpu.registers().readInteger(rt);
                cpu.registers().writeInteger(rd, ~(s | t));
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
                int a = cpu.registers().readInteger(rs);
                int b = cpu.registers().readInteger(rt);
                int res = a - b;
                //overflow check
                if(((a ^ b) & (a ^ res)) < 0) {
                    throw new TrapException(Ov);
                }
                cpu.registers().writeInteger(rd, res);
                return;
            }
            //subu
            case 0b100011: {
                cpu.registers().writeInteger(rd,
                        cpu.registers().readInteger(rs) - cpu.registers().readInteger(rt));
                return;
            }
            //sync, or as i like to call it, fancy nop
            case 0b001111: {
                return;
            }
            //syscall
            case 0b001100: {
                throw new TrapException(Sys);
            }
            //teq
            case 0b110100: {
                if(cpu.registers().readInteger(rs) == cpu.registers().readInteger(rt)) {
                    throw new TrapException(Tr);
                }
                return;
            }
            //tge
            case 0b110000: {
                if(cpu.registers().readInteger(rs) >= cpu.registers().readInteger(rt)) {
                    throw new TrapException(Tr);
                }
                return;
            }
            //tgeu
            case 0b110001: {
                if(Integer.compareUnsigned(cpu.registers().readInteger(rs), cpu.registers().readInteger(rt)) >= 0) {
                    throw new TrapException(Tr);
                }
                return;
            }
            //tlt
            case 0b110010: {
                if(cpu.registers().readInteger(rs) < cpu.registers().readInteger(rt)) {
                    throw new TrapException(Tr);
                }
                return;
            }
            //tltu
            case 0b110011: {
                if(Integer.compareUnsigned(cpu.registers().readInteger(rs), cpu.registers().readInteger(rt)) < 0) {
                    throw new TrapException(Tr);
                }
                return;
            }
            //tne
            case 0b110110: {
                if(cpu.registers().readInteger(rs) != cpu.registers().readInteger(rt)) {
                    throw new TrapException(Tr);
                }
                return;
            }
            //xor
            case 0b100110: {
                cpu.registers().writeInteger(rd,
                        cpu.registers().readInteger(rs) ^ cpu.registers().readInteger(rt));
                return;
            }
            default: throw new TrapException(RI, "Unknown func " + func);
        }
    }

    static String toString(int instruction) {
        int func =   instruction & 0b111111;
        int rd =    (instruction << 16) >>> 27;
        int rs =    (instruction <<  6) >>> 27;
        int rt =    (instruction << 11) >>> 27;
        int shamt = (instruction << 21) >>> 27;
        switch (func) {
            case 0b100000: return "add " + iregs(rd, rs, rt);
            case 0b100001: return "addu " + iregs(rd, rs, rt);
            case 0b100100: return "and " + iregs(rd, rs, rt);
            case 0b001101: return "break";
            case 0b011010: return "div " + iregs(rs, rt);
            case 0b011011: return "divu " + iregs(rs, rt);
            case 0b001001: return "jalr " + iregs(rs, rd);
            case 0b001000: return "jr " + ir(rs);
            case 0b010000: return "mfhi " + ir(rd);
            case 0b010010: return "mflo " + ir(rd);
            case 0b010001: return "mthi " + ir(rs);
            case 0b010011: return "mtlo " + ir(rs);
            case 0b011000: return "mult " + iregs(rs, rt);
            case 0b011001: return "multu " + iregs(rs, rt);
            case 0b100111: return "nor " + iregs(rd, rs, rt);
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
            case 0b001111: return "sync"; // or as i like to call it, fancy nop
            case 0b001100: return "syscall";
            case 0b110100: return "teq " + iregs(rs, rt);
            case 0b110000: return "tge " + iregs(rs, rt);
            case 0b110001: return "tgeu " + iregs(rs, rt);
            case 0b110010: return "tlt " + iregs(rs, rt);
            case 0b110011: return "tltu " + iregs(rs, rt);
            case 0b110110: return "tne " + iregs(rs, rt);
            case 0b100110: return "xor " + iregs(rd, rs, rt);
            default: return invalid(instruction, "Unknown func " + func);
        }
    }
}
