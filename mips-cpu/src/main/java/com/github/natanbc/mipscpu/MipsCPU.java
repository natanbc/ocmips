package com.github.natanbc.mipscpu;

import com.github.natanbc.mipscpu.instruction.MipsInstruction;
import com.github.natanbc.mipscpu.instruction.SyscallHandler;
import com.github.natanbc.mipscpu.memory.MemoryOperationException;
import com.github.natanbc.mipscpu.memory.MemoryHandler;
import com.github.natanbc.mipscpu.memory.MemoryMap;

import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class MipsCPU {
    private static final int BYTE_MASK = 0b11;
    private static final int WORD_MASK = ~BYTE_MASK;

    private final NavigableMap<Integer, MemoryHandler> memoryHandlers = new TreeMap<>(Integer::compareUnsigned);
    private final MipsRegisters registers = new MipsRegisters();
    private final int[] bootRom;
    private int[] memory;
    private SyscallHandler syscallHandler;

    public MipsCPU(int[] bootRom) {
        this.bootRom = bootRom;
        registers.writeInteger(MipsRegisters.PC, MemoryMap.BOOT_ROM_START);
    }

    public MipsRegisters registers() {
        return registers;
    }

    public void setRAM(int[] memory) {
        this.memory = memory;
    }

    public int[] getRAM() {
        return memory;
    }

    public void setSyscallHandler(SyscallHandler syscallHandler) {
        this.syscallHandler = syscallHandler;
    }

    public SyscallHandler getSyscallHandler() {
        return syscallHandler;
    }

    public NavigableMap<Integer, MemoryHandler> memoryHandlers() {
        return memoryHandlers;
    }

    public void addMemoryHandler(int baseAddress, MemoryHandler handler) {
        memoryHandlers.put(baseAddress, handler);
        handler.onAttach(this, baseAddress);
    }

    public MemoryHandler removeMemoryHandler(int address) {
        return memoryHandlers.remove(address);
    }

    public void removeMemoryHandler(MemoryHandler handler) {
        memoryHandlers.entrySet().removeIf(entry -> entry.getValue().equals(handler));
    }

    public void step() throws MipsException {
        registers.clearFlags();
        //haha icache go brrrr
        //jk we don't have those fancy things here
        //we're a simple cpu
        MipsInstruction.execute(this, readWord(registers.readInteger(MipsRegisters.PC)));
        if(!registers.wasPcWritten()) {
            registers.writeInteger(MipsRegisters.PC, registers.readInteger(MipsRegisters.PC) + 4);
        }
    }

    public int readByte(int address) throws MemoryOperationException {
        int word = readWord(address & WORD_MASK);
        int shift = 24 - ((address & BYTE_MASK) << 3);
        return (word >> shift) & 0xFF;
    }

    public int readHalfWord(int address) throws MemoryOperationException {
        int word = readWord(address & ~0b10);
        int shift = 16 * (((address>>>1)&1)^1);
        return (word >> shift) & 0xFFFF;
    }

    public int readWord(int address) throws MemoryOperationException {
        MemoryHandler handler = handlerFor(address);
        if(handler != null) {
            return handler.read(this, address);
        }
        if(isRAM(address)) {
            return memory[(address - MemoryMap.RAM_START)>>2];
        }
        if(isBootRom(address)) {
            return bootRom[(address - MemoryMap.BOOT_ROM_START)>>2];
        }
        throw notMapped(address);
    }

    public void writeByte(int address, int value) throws MemoryOperationException {
        int wordAddress = address & WORD_MASK;
        int word = readWord(wordAddress);
        int shift = 24 - ((address & BYTE_MASK) << 3);
        word &= ~(0xFF<<shift);
        word |= ((value&0xFF)<<shift);
        writeWord(wordAddress, word);
    }

    public void writeHalfWord(int address, int value) throws MemoryOperationException {
        int wordAddress = address & WORD_MASK;
        int word = readWord(wordAddress);
        int shift = 16 * (((address>>>1)&1)^1);
        word &= ~(0xFFFF<<shift);
        word |= ((value&0xFFFF)<<shift);
        writeWord(wordAddress, word);
    }

    public void writeWord(int address, int value) throws MemoryOperationException {
        if(registers.readInteger(MipsRegisters.RMW_ADDRESS) == address) {
            registers.endAtomicUpdate();
        }
        MemoryHandler handler = handlerFor(address);
        if(handler != null) {
            handler.write(this, address, value);
            return;
        }
        if(isRAM(address)) {
            memory[(address - MemoryMap.RAM_START)>>2] = value;
            return;
        }
        if(isBootRom(address)) {
            throw readOnly(address);
        }
        throw notMapped(address);
    }

    public MemoryHandler handlerFor(int address) {
        SortedMap<Integer, MemoryHandler> map = memoryHandlers.headMap(address, true);
        if(map.isEmpty()) {
            return null;
        }
        Integer last = map.lastKey();
        MemoryHandler handler = map.get(map.lastKey());
        if(handler == null || Integer.compareUnsigned(address, last + handler.memorySize()) > 0) {
            return null;
        }
        return handler;
    }

    private boolean isRAM(int address) {
        return memory != null && address >= MemoryMap.RAM_START && address < MemoryMap.RAM_START + (memory.length * 4);
    }

    private boolean isBootRom(int address) {
        return address >= MemoryMap.BOOT_ROM_START && address < MemoryMap.BOOT_ROM_START + (bootRom.length * 4);
    }

    private static MemoryOperationException notMapped(int address) {
        return new MemoryOperationException(address, MemoryOperationException.Reason.NOT_MAPPED);
    }

    private static MemoryOperationException readOnly(int address) {
        return new MemoryOperationException(address, MemoryOperationException.Reason.READ_ONLY);
    }
}
