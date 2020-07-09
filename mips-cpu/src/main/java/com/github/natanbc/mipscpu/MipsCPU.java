package com.github.natanbc.mipscpu;

import com.github.natanbc.mipscpu.instruction.MipsInstruction;
import com.github.natanbc.mipscpu.instruction.SyscallHandler;
import com.github.natanbc.mipscpu.memory.IllegalAddressException;
import com.github.natanbc.mipscpu.memory.MemoryHandler;
import com.github.natanbc.mipscpu.memory.MemoryMap;

import java.util.SortedMap;
import java.util.TreeMap;

public class MipsCPU {
    private static final int BYTE_MASK = 0b11;
    private static final int WORD_MASK = ~BYTE_MASK;

    private final SortedMap<Integer, MemoryHandler> memoryHandlers = new TreeMap<>();
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
        MipsInstruction i = MipsInstruction.decode(readWord(registers.readInteger(MipsRegisters.PC)));
        i.execute(this);
        if(!registers.wasPcWritten()) {
            registers.writeInteger(MipsRegisters.PC, registers.readInteger(MipsRegisters.PC) + 4);
        }
    }

    public int readByte(int address) throws IllegalAddressException {
        int word = readWord(address & WORD_MASK);
        int shift = 24 - ((address & BYTE_MASK) << 3);
        return (word >> shift) & 0xFF;
    }

    public int readWord(int address) throws IllegalAddressException {
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

    public void writeByte(int address, int value) throws IllegalAddressException {
        int wordAddress = address & WORD_MASK;
        int word = readWord(wordAddress);
        int shift = 24 - ((address & BYTE_MASK) << 3);
        word &= ~(0xFF<<shift);
        word |= ((value&0xFF)<<shift);
        writeWord(wordAddress, word);
    }

    public void writeWord(int address, int value) throws IllegalAddressException {
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

    private boolean isRAM(int address) {
        return memory != null && address >= MemoryMap.RAM_START && address < MemoryMap.RAM_START + (memory.length * 4);
    }

    private boolean isBootRom(int address) {
        return address >= MemoryMap.BOOT_ROM_START && address < MemoryMap.BOOT_ROM_START + (bootRom.length * 4);
    }

    private MemoryHandler handlerFor(int address) {
        SortedMap<Integer, MemoryHandler> map = memoryHandlers.headMap(address+1);
        if(map.isEmpty()) {
            return null;
        }
        Integer last = map.lastKey();
        MemoryHandler handler = map.get(map.lastKey());
        if(handler == null || address > last + handler.memorySize()) {
            return null;
        }
        return handler;
    }

    private static IllegalAddressException notMapped(int address) {
        return new IllegalAddressException(address, IllegalAddressException.Reason.NOT_MAPPED);
    }

    private static IllegalAddressException readOnly(int address) {
        return new IllegalAddressException(address, IllegalAddressException.Reason.READ_ONLY);
    }
}
