package com.github.natanbc.mipscpu.instruction;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.MipsException;

import static com.github.natanbc.mipscpu.MipsRegisters.*;
import static com.github.natanbc.mipscpu.instruction.MipsInstruction.*;
import static com.github.natanbc.mipscpu.instruction.TrapException.Cause.*;

public class COP0 {
    static void execute(MipsCPU cpu, int instruction) throws MipsException {
        boolean co = ((instruction >>> 25) & 0b1) == 0b1;
        if(co) {
            int op = instruction & 0b111111;
            switch (op) {
                //deret
                case 0b011111: {
                    throw new TrapException(RI, "deret");
                }
                //eret
                case 0b011000: {
                    cpu.registers().endAtomicUpdate();
                    cpu.registers().writeInteger(PC, cpu.registers().readCop0(C0_EPC));
                    cpu.registers().writeStatusBit(STATUS_EXCEPTION_LEVEL, false);
                    return;
                }
                //tlbp
                case 0b001000:
                    //tlbr
                case 0b000001:
                    //tlbwi
                case 0b000010:
                    //tlbwr
                case 0b000110: {
                    throw new TrapException(RI, "TLB unimplemented");
                }
                //wait
                case 0b100000: {
                    return;
                }
                default: throw new TrapException(RI, "Unknown COP0 CO " + Integer.toBinaryString(op));
            }
        } else {
            int op = (instruction >>> 21) & 0b11111;
            int rt = (instruction >>> 16) & 0b11111;
            int rd = (instruction >>> 11) & 0b11111;
            switch (op) {
                //mfc0
                case 0b00000: {
                    cpu.registers().writeInteger(rt, cpu.registers().readCop0(rd));
                    return;
                }
                //mtc0
                case 0b00100: {
                    cpu.registers().writeCop0(rd, cpu.registers().readInteger(rt));
                    return;
                }
                default: throw new TrapException(RI, "Unknown COP0 " + Integer.toBinaryString(op));
            }
        }
    }

    static String toString(int instruction) {
        boolean co = ((instruction >>> 25) & 0b1) == 0b1;
        if(co) {
            int op = instruction & 0b111111;
            switch (op) {
                case 0b011111: return "deret";
                case 0b011000: return "eret";
                case 0b001000: return "tlbp";
                case 0b000001: return "tlbr";
                case 0b000010: return "tlbwi";
                case 0b000110: return "tlbwr";
                //wait
                case 0b100000: return "wait";
                default: return invalid(instruction, "Unknown COP0 CO " + Integer.toBinaryString(op));
            }
        } else {
            int op = (instruction >>> 21) & 0b11111;
            int rt = (instruction >>> 16) & 0b11111;
            int rd = (instruction >>> 11) & 0b11111;
            switch (op) {
                //mfc0
                case 0b00000: return "mfc0 " + ir(rt) + ", $" + rd;
                //mtc0
                case 0b00100: return "mtc0 " + ir(rt) + ", $" + rd;
                default: return invalid(instruction, "Unknown COP0 " + Integer.toBinaryString(op));
            }
        }
    }
}
