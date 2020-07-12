package com.github.natanbc.ocmips.utils;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.MipsRegisters;
import com.github.natanbc.mipscpu.memory.MemoryOperationException;
import com.github.natanbc.ocmips.handlers.DriveHandler;
import com.github.natanbc.ocmips.handlers.EEPROMHandler;
import com.github.natanbc.ocmips.handlers.FramebufferHandler;
import li.cil.oc.api.machine.Machine;

import java.util.HashMap;
import java.util.Map;

public class SpecialComponentMapper {
    private static final Map<Integer, String> TYPES = new HashMap<>();
    
    static {
        TYPES.put(0, "gpu");
        TYPES.put(1, "eeprom");
        TYPES.put(2, "drive");
    }
    
    public static void map(Machine machine, MipsCPU cpu) throws MemoryOperationException {
        int addr = cpu.registers().readInteger(MipsRegisters.A0);
        String component = MemoryUtils.readAddress(cpu, cpu.registers().readInteger(MipsRegisters.A1));
        int type = cpu.registers().readInteger(MipsRegisters.A2);
        int flags = cpu.registers().readInteger(MipsRegisters.A3);
        
        String actualType = machine.components().get(component);
        String expectedType = TYPES.get(type);
        if(actualType == null) {
            cpu.registers().writeInteger(MipsRegisters.V0, -1);
            return;
        }
        if(expectedType == null) {
            cpu.registers().writeInteger(MipsRegisters.V0, -2);
            return;
        }
        if(!actualType.equals(expectedType)) {
            cpu.registers().writeInteger(MipsRegisters.V0, -3);
            return;
        }
        
        switch(type) {
            case 0: mapGpu(machine, cpu, addr, component); return;
            case 1: mapEeprom(machine, cpu, addr, component); return;
            case 2: mapDrive(machine, cpu, addr, component, flags); return;
            default: throw new AssertionError();
        }
    }
    
    public static void mapGpu(Machine machine, MipsCPU cpu, int addr, String gpuAddress) {
        try {
            Object[] max = machine.invoke(gpuAddress, "getResolution", new Object[0]);
            int width = (Integer)max[0];
            int height = (Integer)max[1];
            Object[] buffer = machine.invoke(gpuAddress, "allocateBuffer", new Object[0]);
            int bufferNumber = (Integer)buffer[0];
            machine.invoke(gpuAddress, "setActiveBuffer", new Object[] { bufferNumber });
            cpu.addMemoryHandler(addr, new FramebufferHandler(machine, gpuAddress, width, height, bufferNumber));
            cpu.registers().writeInteger(MipsRegisters.V0, 0);
        } catch (Exception e) {
            cpu.registers().writeInteger(MipsRegisters.V0, -4);
        }
    }
    
    public static void mapEeprom(Machine machine, MipsCPU cpu, int addr, String eepromAddress) {
        try {
            int size = (Integer)machine.invoke(eepromAddress, "getSize", new Object[0])[0];
            int dataSize = (Integer)machine.invoke(eepromAddress, "getDataSize", new Object[0])[0];
            cpu.addMemoryHandler(addr, new EEPROMHandler(machine, eepromAddress, size, dataSize));
            cpu.registers().writeInteger(MipsRegisters.V0, 0);
        } catch(Exception e) {
            cpu.registers().writeInteger(MipsRegisters.V0, -4);
        }
    }

    public static void mapDrive(Machine machine, MipsCPU cpu, int addr, String driveAddr, int flags) {
        try {
            int size = (Integer)machine.invoke(driveAddr, "getCapacity", new Object[0])[0];
            int sectorSize = (Integer)machine.invoke(driveAddr, "getSectorSize", new Object[0])[0];
            cpu.addMemoryHandler(addr, new DriveHandler(machine, driveAddr, flags, size, sectorSize));
            cpu.registers().writeInteger(MipsRegisters.V0, 0);
        } catch(Exception e) {
            cpu.registers().writeInteger(MipsRegisters.V0, -4);
        }
    }
}
