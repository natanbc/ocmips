package com.github.natanbc.mipscpu.instruction;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.MipsException;

import static com.github.natanbc.mipscpu.MipsRegisters.*;
import static com.github.natanbc.mipscpu.instruction.MipsInstruction.*;
import static com.github.natanbc.mipscpu.instruction.TrapException.Cause.*;

class RegImm {
    static void execute(MipsCPU cpu, int instruction) throws MipsException {
        int op = (instruction << 11) >>> 27;
        int rs =  (instruction <<  6) >>> 27;
        int imm = instruction & 0xFFFF;
        switch (op) {
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
            //teqi
            case 0b01100: {
                if(cpu.registers().readInteger(rs) == signExtend(imm)) {
                    throw new TrapException(Tr);
                }
                return;
            }
            //tgei
            case 0b01000: {
                if(cpu.registers().readInteger(rs) >= signExtend(imm)) {
                    throw new TrapException(Tr);
                }
                return;
            }
            //tgeiu
            case 0b01001: {
                if(Integer.compareUnsigned(cpu.registers().readInteger(rs), signExtend(imm)) >= 0) {
                    throw new TrapException(Tr);
                }
                return;
            }
            //tlti
            case 0b01010: {
                if(cpu.registers().readInteger(rs) < signExtend(imm)) {
                    throw new TrapException(Tr);
                }
                return;
            }
            //tltiu
            case 0b01011: {
                if(Integer.compareUnsigned(cpu.registers().readInteger(rs), signExtend(imm)) < 0) {
                    throw new TrapException(Tr);
                }
                return;
            }
            //tnei
            case 0b01110: {
                if(cpu.registers().readInteger(rs) != signExtend(imm)) {
                    throw new TrapException(Tr);
                }
                return;
            }
            default: throw new TrapException(RI, "Unknown regimm " + Integer.toBinaryString(op));
        }
    }

    static String toString(int instruction) {
        int op = (instruction << 11) >>> 27;
        int rs =  (instruction <<  6) >>> 27;
        int imm = instruction & 0xFFFF;
        switch(op) {
            case 0b00001: return "bgez " + ir(rs) + ", " + signExtend(imm);
            case 0b10001: return "bgezal " + ir(rs) + ", " + signExtend(imm);
            case 0b00000: return "bltz " + ir(rs) + ", " + signExtend(imm);
            case 0b10000: return "bltzal " + ir(rs) + ", " + signExtend(imm);
            case 0b01100: return "teqi " + ir(rs) + ", " + signExtend(imm);
            case 0b01000: return "tgei " + ir(rs) + ", " + signExtend(imm);
            case 0b01001: return "tgeiu " + ir(rs) + ", " + signExtend(imm);
            case 0b01010: return "tlti " + ir(rs) + ", " + signExtend(imm);
            case 0b01011: return "tltiu " + ir(rs) + ", " + signExtend(imm);
            case 0b01110: return "tnei " + ir(rs) + ", " + signExtend(imm);
            default: return invalid(instruction, "Unknown regimm " + Integer.toBinaryString(op));
        }
    }
}
