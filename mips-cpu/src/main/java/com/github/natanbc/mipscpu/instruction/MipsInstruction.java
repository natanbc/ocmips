package com.github.natanbc.mipscpu.instruction;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.MipsException;

import static com.github.natanbc.mipscpu.MipsRegisters.*;

public interface MipsInstruction {
    void execute(MipsCPU cpu) throws MipsException;

    static MipsInstruction decode(int encoding) {
        return MipsDecoder.decode(encoding);
    }

    //http://www.mrc.uidaho.edu/mrc/people/jff/digital/MIPSir.html

    class Add implements MipsInstruction {
        private final int d, s, t;

        public Add(int d, int s, int t) {
            this.d = d;
            this.s = s;
            this.t = t;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(d, cpu.registers().readInteger(s) + cpu.registers().readInteger(t));
            //TODO overflow check
        }

        @Override
        public String toString() {
            return "add $" + d + ", $" + s + ", $" + t;
        }
    }

    class Addi implements MipsInstruction {
        private final int t, s, imm;

        public Addi(int t, int s, int imm) {
            this.t = t;
            this.s = s;
            this.imm = imm;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(t, cpu.registers().readInteger(s) + imm);
            //TODO overflow check
        }

        @Override
        public String toString() {
            return "addi $" + t + ", $" + s + ", " + imm;
        }
    }

    class Addiu implements MipsInstruction {
        private final int t, s, imm;

        public Addiu(int t, int s, int imm) {
            this.t = t;
            this.s = s;
            this.imm = imm;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(t, cpu.registers().readInteger(s) + imm);
        }

        @Override
        public String toString() {
            return "addiu $" + t + ", $" + s + ", " + imm;
        }
    }

    class Addu implements MipsInstruction {
        private final int d, s, t;

        public Addu(int d, int s, int t) {
            this.d = d;
            this.s = s;
            this.t = t;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(d, cpu.registers().readInteger(s) + cpu.registers().readInteger(t));
        }

        @Override
        public String toString() {
            return "addu $" + d + ", $" + s + ", $" + t;
        }
    }

    class And implements MipsInstruction {
        private final int d, s, t;

        public And(int d, int s, int t) {
            this.d = d;
            this.s = s;
            this.t = t;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(d, cpu.registers().readInteger(s) & cpu.registers().readInteger(t));
        }

        @Override
        public String toString() {
            return "and $" + d + ", $" + s + ", $" + t;
        }
    }

    class Andi implements MipsInstruction {
        private final int t, s, imm;

        public Andi(int t, int s, int imm) {
            this.t = t;
            this.s = s;
            this.imm = imm;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(t, cpu.registers().readInteger(s) & imm);
        }

        @Override
        public String toString() {
            return "andi $" + t + ", $" + s + ", " + imm;
        }
    }

    class Beq implements MipsInstruction {
        private final int s, t, offset;

        public Beq(int s, int t, int offset) {
            this.s = s;
            this.t = t;
            this.offset = offset;
        }

        @Override
        public void execute(MipsCPU cpu) {
            if(cpu.registers().readInteger(s) == cpu.registers().readInteger(t)) {
                cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + (offset << 2));
            }
        }

        @Override
        public String toString() {
            return "beq $" + s + ", $" + t + ", " + offset;
        }
    }

    class Bgez implements MipsInstruction {
        private final int s, offset;

        public Bgez(int s, int offset) {
            this.s = s;
            this.offset = offset;
        }

        @Override
        public void execute(MipsCPU cpu) {
            if(cpu.registers().readInteger(s) >= 0) {
                cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + (offset << 2));
            }
        }

        @Override
        public String toString() {
            return "bgez $" + s + ", " + offset;
        }
    }

    class Bgezal implements MipsInstruction {
        private final int s, offset;

        public Bgezal(int s, int offset) {
            this.s = s;
            this.offset = offset;
        }

        @Override
        public void execute(MipsCPU cpu) {
            if(cpu.registers().readInteger(s) >= 0) {
                cpu.registers().writeInteger(RA, cpu.registers().readInteger(PC) + 8);
                cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + (offset << 2));
            }
        }

        @Override
        public String toString() {
            return "bgezal $" + s + ", " + offset;
        }
    }

    class Bgtz implements MipsInstruction {
        private final int s, offset;

        public Bgtz(int s, int offset) {
            this.s = s;
            this.offset = offset;
        }

        @Override
        public void execute(MipsCPU cpu) {
            if(cpu.registers().readInteger(s) > 0) {
                cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + (offset << 2));
            }
        }

        @Override
        public String toString() {
            return "bgtz $" + s + ", " + offset;
        }
    }

    class Blez implements MipsInstruction {
        private final int s, offset;

        public Blez(int s, int offset) {
            this.s = s;
            this.offset = offset;
        }

        @Override
        public void execute(MipsCPU cpu) {
            if(cpu.registers().readInteger(s) <= 0) {
                cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + (offset << 2));
            }
        }

        @Override
        public String toString() {
            return "blez $" + s + ", " + offset;
        }
    }

    class Bltz implements MipsInstruction {
        private final int s, offset;

        public Bltz(int s, int offset) {
            this.s = s;
            this.offset = offset;
        }

        @Override
        public void execute(MipsCPU cpu) {
            if(cpu.registers().readInteger(s) < 0) {
                cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + (offset << 2));
            }
        }

        @Override
        public String toString() {
            return "bltz $" + s + ", " + offset;
        }
    }

    class Bltzal implements MipsInstruction {
        private final int s, offset;

        public Bltzal(int s, int offset) {
            this.s = s;
            this.offset = offset;
        }

        @Override
        public void execute(MipsCPU cpu) {
            if(cpu.registers().readInteger(s) < 0) {
                cpu.registers().writeInteger(RA, cpu.registers().readInteger(PC) + 8);
                cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + (offset << 2));
            }
        }

        @Override
        public String toString() {
            return "bltzal $" + s + ", " + offset;
        }
    }

    class Bne implements MipsInstruction {
        private final int s, t, offset;

        public Bne(int s, int t, int offset) {
            this.s = s;
            this.t = t;
            this.offset = offset;
        }

        @Override
        public void execute(MipsCPU cpu) {
            if(cpu.registers().readInteger(s) != cpu.registers().readInteger(t)) {
                cpu.registers().writeInteger(PC, cpu.registers().readInteger(PC) + (offset << 2));
            }
        }

        @Override
        public String toString() {
            return "bne $" + s + ", $" + t + ", " + offset;
        }
    }

    class Div implements MipsInstruction {
        private final int s, t;

        public Div(int s, int t) {
            this.s = s;
            this.t = t;
        }

        @Override
        public void execute(MipsCPU cpu) {
            int a = cpu.registers().readInteger(s);
            int b = cpu.registers().readInteger(t);
            cpu.registers().writeInteger(LO, a / b);
            cpu.registers().writeInteger(HI, a % b);
        }

        @Override
        public String toString() {
            return "div $" + s + ", $" + t;
        }
    }

    class Divu implements MipsInstruction {
        private final int s, t;

        public Divu(int s, int t) {
            this.s = s;
            this.t = t;
        }

        @Override
        public void execute(MipsCPU cpu) {
            int a = cpu.registers().readInteger(s);
            int b = cpu.registers().readInteger(t);
            cpu.registers().writeInteger(LO, Integer.divideUnsigned(a, b));
            cpu.registers().writeInteger(HI, Integer.remainderUnsigned(a, b));
        }

        @Override
        public String toString() {
            return "divu $" + s + ", $" + t;
        }
    }

    class J implements MipsInstruction {
        private final int offset;

        public J(int offset) {
            this.offset = offset;
        }

        @Override
        public void execute(MipsCPU cpu) {
            int pc = cpu.registers().readInteger(PC);
            //PC = nPC;
            pc += 4;
            //nPC = (PC & 0xf0000000) | (target << 2)
            cpu.registers().writeInteger(PC, (pc & 0xf0000000) | (offset << 2));
        }

        @Override
        public String toString() {
            return "j " + offset;
        }
    }

    class Jal implements MipsInstruction {
        private final int offset;

        public Jal(int offset) {
            this.offset = offset;
        }

        @Override
        public void execute(MipsCPU cpu) {
            int pc = cpu.registers().readInteger(PC);
            cpu.registers().writeInteger(RA, pc + 8);
            //PC = nPC;
            pc += 4;
            //nPC = (PC & 0xf0000000) | (target << 2)
            cpu.registers().writeInteger(PC, (pc & 0xf0000000) | (offset << 2));
        }

        @Override
        public String toString() {
            return "jal " + offset;
        }
    }

    class Jalr implements MipsInstruction {
        private final int s, d;

        public Jalr(int s, int d) {
            this.s = s;
            this.d = d;
        }

        @Override
        public void execute(MipsCPU cpu) {
            int temp = cpu.registers().readInteger(s);
            cpu.registers().writeInteger(d, cpu.registers().readInteger(PC) + 8);
            cpu.registers().writeInteger(PC, temp);
        }

        @Override
        public String toString() {
            return "jalr $" + s + ", $" + d;
        }
    }

    class Jr implements MipsInstruction {
        private final int s;

        public Jr(int s) {
            this.s = s;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(PC, cpu.registers().readInteger(s));
        }

        @Override
        public String toString() {
            return "jr $" + s;
        }
    }

    class Lb implements MipsInstruction {
        private final int t, offset, s;

        public Lb(int t, int offset, int s) {
            this.t = t;
            this.offset = offset;
            this.s = s;
        }

        @Override
        public void execute(MipsCPU cpu) throws MipsException {
            int address = cpu.registers().readInteger(s) + offset;
            cpu.registers().writeInteger(t, cpu.readByte(address));
        }

        @Override
        public String toString() {
            return "lb $" + t + ", " + offset + "($" + s + ")";
        }
    }

    class Lui implements MipsInstruction {
        private final int t, imm;

        public Lui(int t, int imm) {
            this.t = t;
            this.imm = imm;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(t, imm << 16);
        }

        @Override
        public String toString() {
            return "lui $" + t + ", " + imm;
        }
    }

    class Lw implements MipsInstruction {
        private final int t, offset, s;

        public Lw(int t, int offset, int s) {
            this.t = t;
            this.offset = offset;
            this.s = s;
        }

        @Override
        public void execute(MipsCPU cpu) throws MipsException {
            int addr = cpu.registers().readInteger(s) + offset;
            if((addr & ~0b11) != addr) {
                throw new InstructionExecutionException("Unaligned memory read to 0x" + Integer.toHexString(addr));
            }
            cpu.registers().writeInteger(t, cpu.readWord(addr));
        }

        @Override
        public String toString() {
            return "lw $" + t + ", " + offset + "($" + s + ")";
        }
    }
    
    class Mfhi implements MipsInstruction {
        private final int d;

        public Mfhi(int d) {
            this.d = d;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(d, cpu.registers().readInteger(HI));
        }

        @Override
        public String toString() {
            return "mfhi $" + d;
        }
    }

    class Mflo implements MipsInstruction {
        private final int d;

        public Mflo(int d) {
            this.d = d;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(d, cpu.registers().readInteger(LO));
        }

        @Override
        public String toString() {
            return "mflo $" + d;
        }
    }

    class Mult implements MipsInstruction {
        private final int s, t;

        public Mult(int s, int t) {
            this.s = s;
            this.t = t;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(LO, cpu.registers().readInteger(s) * cpu.registers().readInteger(t));
            //TODO overflow check
        }

        @Override
        public String toString() {
            return "mult $" + s + ", $" + t;
        }
    }

    class Multu implements MipsInstruction {
        private final int s, t;

        public Multu(int s, int t) {
            this.s = s;
            this.t = t;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(LO, cpu.registers().readInteger(s) * cpu.registers().readInteger(t));
        }

        @Override
        public String toString() {
            return "multu $" + s + ", $" + t;
        }
    }

    class Or implements MipsInstruction {
        private final int d, s, t;

        public Or(int d, int s, int t) {
            this.d = d;
            this.s = s;
            this.t = t;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(d, cpu.registers().readInteger(s) | cpu.registers().readInteger(t));
        }

        @Override
        public String toString() {
            return "or $" + d + ", $" + s + ", $" + t;
        }
    }

    class Ori implements MipsInstruction {
        private final int t, s, imm;

        public Ori(int t, int s, int imm) {
            this.t = t;
            this.s = s;
            this.imm = imm;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(t, cpu.registers().readInteger(s) | imm);
        }

        @Override
        public String toString() {
            return "ori $" + t + ", $" + s + ", " + imm;
        }
    }

    class Sb implements MipsInstruction {
        private final int t, offset, s;

        public Sb(int t, int offset, int s) {
            this.t = t;
            this.offset = offset;
            this.s = s;
        }

        @Override
        public void execute(MipsCPU cpu) throws MipsException {
            cpu.writeByte(s + offset, 0xFF & cpu.registers().readInteger(t));
        }

        @Override
        public String toString() {
            return "sb $" + t + ", " + offset + "($" + s + ")";
        }
    }

    class Sll implements MipsInstruction {
        private final int d, t, h;

        public Sll(int d, int t, int h) {
            this.d = d;
            this.t = t;
            this.h = h;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(d, cpu.registers().readInteger(t) << h);
        }

        @Override
        public String toString() {
            return "sll $" + d + ", $" + t + ", " + h;
        }
    }

    class Sllv implements MipsInstruction {
        private final int d, t, s;

        public Sllv(int d, int t, int s) {
            this.d = d;
            this.t = t;
            this.s = s;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(d, cpu.registers().readInteger(t) << cpu.registers().readInteger(s));
        }

        @Override
        public String toString() {
            return "sllv $" + d + ", $" + t + ", $" + s;
        }
    }

    class Slt implements MipsInstruction {
        private final int d, s, t;

        public Slt(int d, int s, int t) {
            this.d = d;
            this.s = s;
            this.t = t;
        }

        @Override
        public void execute(MipsCPU cpu) {
            if(cpu.registers().readInteger(s) < cpu.registers().readInteger(t)) {
                cpu.registers().writeInteger(d, 1);
            } else {
                cpu.registers().writeInteger(d, 0);
            }
        }

        @Override
        public String toString() {
            return "slt $" + d + ", $" + s + ", $" + t;
        }
    }

    class Slti implements MipsInstruction {
        private final int t, s, imm;

        public Slti(int t, int s, int imm) {
            this.t = t;
            this.s = s;
            this.imm = imm;
        }

        @Override
        public void execute(MipsCPU cpu) {
            if(cpu.registers().readInteger(s) < imm) {
                cpu.registers().writeInteger(t, 1);
            } else {
                cpu.registers().writeInteger(t, 0);
            }
        }

        @Override
        public String toString() {
            return "slti $" + t + ", $" + s + ", " + imm;
        }
    }

    class Sltiu implements MipsInstruction {
        private final int t, s, imm;

        public Sltiu(int t, int s, int imm) {
            this.t = t;
            this.s = s;
            this.imm = imm;
        }

        @Override
        public void execute(MipsCPU cpu) {
            if(cpu.registers().readInteger(s) < imm) {
                cpu.registers().writeInteger(t, 1);
            } else {
                cpu.registers().writeInteger(t, 0);
            }
        }

        @Override
        public String toString() {
            return "sltiu $" + t + ", $" + s + ", " + imm;
        }
    }

    class Sltu implements MipsInstruction {
        private final int d, s, t;

        public Sltu(int d, int s, int t) {
            this.d = d;
            this.s = s;
            this.t = t;
        }

        @Override
        public void execute(MipsCPU cpu) {
            int cmp = Integer.compareUnsigned(cpu.registers().readInteger(s), cpu.registers().readInteger(t));
            if(cmp < 0) {
                cpu.registers().writeInteger(d, 1);
            } else {
                cpu.registers().writeInteger(d, 0);
            }
        }

        @Override
        public String toString() {
            return "sltu $" + d + ", $" + s + ", $" + t;
        }
    }

    class Sra implements MipsInstruction {
        private final int d, t, h;

        public Sra(int d, int t, int h) {
            this.d = d;
            this.t = t;
            this.h = h;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(d, cpu.registers().readInteger(t) >> h);
        }

        @Override
        public String toString() {
            return "sra $" + d + ", $" + t + ", " + h;
        }
    }

    class Srl implements MipsInstruction {
        private final int d, t, h;

        public Srl(int d, int t, int h) {
            this.d = d;
            this.t = t;
            this.h = h;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(d, cpu.registers().readInteger(t) >>> h);
        }

        @Override
        public String toString() {
            return "srl $" + d + ", $" + t + ", " + h;
        }
    }

    class Srlv implements MipsInstruction {
        private final int d, t, s;

        public Srlv(int d, int t, int s) {
            this.d = d;
            this.t = t;
            this.s = s;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(d, cpu.registers().readInteger(t) >>> cpu.registers().readInteger(s));
        }

        @Override
        public String toString() {
            return "srlv $" + d + ", $" + t + ", $" + s;
        }
    }

    class Sub implements MipsInstruction {
        private final int d, s, t;

        public Sub(int d, int s, int t) {
            this.d = d;
            this.s = s;
            this.t = t;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(d, cpu.registers().readInteger(s) - cpu.registers().readInteger(t));
            //TODO overflow check
        }

        @Override
        public String toString() {
            return "sub $" + d + ", $" + s + ", $" + t;
        }
    }

    class Subu implements MipsInstruction {
        private final int d, s, t;

        public Subu(int d, int s, int t) {
            this.d = d;
            this.s = s;
            this.t = t;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(d, cpu.registers().readInteger(s) - cpu.registers().readInteger(t));
        }

        @Override
        public String toString() {
            return "subu $" + d + ", $" + s + ", $" + t;
        }
    }

    class Sw implements MipsInstruction {
        private final int t, offset, s;

        public Sw(int t, int offset, int s) {
            this.t = t;
            this.offset = offset;
            this.s = s;
        }

        @Override
        public void execute(MipsCPU cpu) throws MipsException {
            int addr = cpu.registers().readInteger(s) + offset;
            if((addr & ~0b11) != addr) {
                throw new InstructionExecutionException("Unaligned memory write to 0x" + Integer.toHexString(addr));
            }
            cpu.writeWord(addr, cpu.registers().readInteger(t));
        }

        @Override
        public String toString() {
            return "sw $" + t + ", " + offset + "($" + s + ")";
        }
    }

    class Syscall implements MipsInstruction {
        @Override
        public void execute(MipsCPU cpu) throws MipsException {
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
        }

        @Override
        public String toString() {
            return "syscall";
        }
    }

    class Xor implements MipsInstruction {
        private final int d, s, t;

        public Xor(int d, int s, int t) {
            this.d = d;
            this.s = s;
            this.t = t;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(d, cpu.registers().readInteger(s) ^ cpu.registers().readInteger(t));
        }

        @Override
        public String toString() {
            return "xor $" + d + ", $" + s + ", $" + t;
        }
    }

    class Xori implements MipsInstruction {
        private final int t, s, imm;

        public Xori(int t, int s, int imm) {
            this.t = t;
            this.s = s;
            this.imm = imm;
        }

        @Override
        public void execute(MipsCPU cpu) {
            cpu.registers().writeInteger(t, cpu.registers().readInteger(s) ^ imm);
        }

        @Override
        public String toString() {
            return "xori $" + t + ", $" + s + ", " + imm;
        }
    }

    class Invalid implements MipsInstruction {
        private final int encoding;
        private final String reason;

        public Invalid(int encoding, String reason) {
            this.encoding = encoding;
            this.reason = reason;
        }

        @Override
        public void execute(MipsCPU cpu) throws MipsException {
            throw new IllegalInstructionException(encoding, reason);
        }

        @Override
        public String toString() {
            return "Invalid instruction " + Integer.toHexString(encoding) + ": " + reason;
        }
    }
}
