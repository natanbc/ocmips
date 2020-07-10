package com.github.natanbc.ocmips;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.MipsException;
import com.github.natanbc.mipscpu.MipsRegisters;
import com.github.natanbc.mipscpu.instruction.MipsInstruction;
import com.github.natanbc.mipscpu.memory.MemoryHandler;
import com.github.natanbc.mipscpu.memory.MemoryMap;
import com.github.natanbc.ocmips.handlers.CleanableHandler;
import com.github.natanbc.ocmips.handlers.ComponentCallHandler;
import com.github.natanbc.ocmips.utils.BSOD;
import com.github.natanbc.ocmips.utils.MemoryUtils;
import com.github.natanbc.ocmips.utils.SpecialComponentMapper;
import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Memory;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.Architecture;
import li.cil.oc.api.machine.ExecutionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Architecture.Name("MIPS")
@Architecture.NoMemoryRequirements
public class MipsArchitecture implements Architecture {
    private static final int[] BOOTROM = {
            // jump to ram start
            0x3c010000 | (MemoryMap.RAM_START >>> 16),
            0x00200008
    };

    private static final int MAX_STEPS_PER_CALL = 1500;

    private final Machine machine;
    private int ramWords;
    private MipsCPU cpu;
    private boolean booted;
    private volatile ExecutionResult queuedResult;
    private boolean crashed;

    private String gpuAddress, screenAddress;

    public MipsArchitecture(Machine machine) {
        this.machine = machine;
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public boolean recomputeMemory(Iterable<ItemStack> components) {
        int bytes = 0;

        for(ItemStack stack : components) {
            DriverItem d = Driver.driverFor(stack);

            if(d instanceof Memory) {
                Memory m = (Memory)d;
                double amtf = m.amount(stack);
                bytes += ((int)amtf);
            }
        }

        ramWords = ((bytes*1024)+3)>>>2;
        return true;
    }

    @Override
    public boolean initialize() {
        cpu = new MipsCPU(BOOTROM);
        booted = false;
        crashed = false;
        MemoryHandler badApple = OCMips.badApple();
        if(badApple != null) {
            cpu.addMemoryHandler(0x13370000, badApple);
        }
        cpu.setSyscallHandler(cpu -> {
            switch (cpu.registers().readInteger(MipsRegisters.V0)) {
                //sleep
                case 1: throw new StopExecution(new ExecutionResult.Sleep(
                        cpu.registers().readInteger(MipsRegisters.A0)
                ));
                //shutdown
                case 2: throw new StopExecution(new ExecutionResult.Shutdown(
                        cpu.registers().readInteger(MipsRegisters.A0) != 0
                ));
                //map special component
                case 3: {
                    SpecialComponentMapper.map(machine, cpu);
                    return;
                }
                //unmap region
                case 4: {
                    int addr = cpu.registers().readInteger(MipsRegisters.A0);
                    MemoryHandler m = cpu.removeMemoryHandler(addr);
                    if(m == null) return;
                    if(m instanceof CleanableHandler) {
                        ((CleanableHandler)m).cleanup(cpu);
                    }
                    return;
                }
                //map component call
                case 5: {
                    int addr = cpu.registers().readInteger(MipsRegisters.A0);
                    String component = MemoryUtils.readAddress(cpu, cpu.registers().readInteger(MipsRegisters.A1));
                    String method = MemoryUtils.readString(cpu, cpu.registers().readInteger(MipsRegisters.A2));
                    int maxArg = cpu.registers().readInteger(MipsRegisters.A3);
                    cpu.addMemoryHandler(addr, new ComponentCallHandler(machine, component, method, maxArg));
                    return;
                }
                //find Nth component by type
                case 6: {
                    String type = MemoryUtils.readString(cpu, cpu.registers().readInteger(MipsRegisters.A0));
                    int which = cpu.registers().readInteger(MipsRegisters.A1);
                    int addr = cpu.registers().readInteger(MipsRegisters.A2);
                    Optional<String> component = machine.components().entrySet()
                            .stream()
                            .filter(e -> e.getValue().equals(type))
                            .map(Map.Entry::getKey)
                            .sorted()
                            .skip(which)
                            .findFirst();
                    if(component.isPresent()) {
                        cpu.registers().writeInteger(MipsRegisters.V0, 0);
                        IntBuffer buffer = MemoryUtils.toBuffer(UUID.fromString(component.get()));
                        for(int i = 0; i < buffer.capacity(); i++) {
                            cpu.writeWord(addr + i * 4, buffer.get(i));
                        }
                    } else {
                        cpu.registers().writeInteger(MipsRegisters.V0, 1);
                    }
                    return;
                }
                default: cpu.registers().writeInteger(MipsRegisters.V0, -1);
            }
        });
        return true;
    }

    @Override
    public void close() {

    }

    @Override
    public void runSynchronized() {
        updateRam();
        if(!booted) {
            try {
                boot();
                if(!crashed) {
                    booted = true;
                }
            } catch (Exception e) {
                bsod("ERR_FAIL_BOOT");
            }
            return;
        }
        try {
            for(int i = 0; i < MAX_STEPS_PER_CALL; i += 10) {
                cpu.step();
            }
        } catch (MipsException e) {
            queuedResult = new ExecutionResult.Error(e.getMessage());
        } catch (StopExecution e) {
            queuedResult = e.getReason();
        } catch (RetryInNextTick ignored) {
        }
    }

    @Override
    public ExecutionResult runThreaded(boolean isSynchronizedReturn) {
        if(crashed) {
            return new ExecutionResult.Sleep(100);
        }
        updateRam();
        ExecutionResult r = queuedResult;
        queuedResult = null;
        if(r != null) return r;
        if(!booted) {
            return new ExecutionResult.SynchronizedCall();
        }
        try {
            for(int i = 0; i < MAX_STEPS_PER_CALL; i++) {
                cpu.step();
            }
            return new ExecutionResult.Sleep(10);
        } catch (MipsException e) {
            e.printStackTrace(new PrintStream(new FileOutputStream(FileDescriptor.err)));
            try {
                int pc;
                System.err.printf("PC=   0x%x\n", pc = cpu.registers().readInteger(MipsRegisters.PC));
                int instr;
                System.err.printf("Instr=0x%x\n", instr = cpu.readWord(pc));
                System.err.printf("Instr=%s\n", MipsInstruction.decode(instr));
            } catch (Exception ignore) {}
            return new ExecutionResult.Error(e.getMessage());
        } catch (StopExecution e) {
            return e.getReason();
        } catch (RetryInNextTick e) {
            return new ExecutionResult.Sleep(1);
        }
    }

    @Override
    public void onSignal() {

    }

    @Override
    public void onConnect() {

    }

    @Override
    public void load(NBTTagCompound tag) {}

    @Override
    public void save(NBTTagCompound tag) {}

    private void updateRam() {
        if(cpu != null && cpu.getRAM() != null && cpu.getRAM().length != ramWords) {
            int[] n = new int[ramWords];
            System.arraycopy(cpu.getRAM(), 0, n, 0, Math.min(n.length, cpu.getRAM().length));
            cpu.setRAM(n);
        }
    }

    private void boot() throws Exception {
        machine.beep((short)400, (short)20);
        String eepromAddress = null;
        Map<String, String> m = this.machine.components();
        for(String k: m.keySet()) {
            String v = m.get(k);
            if(v.equals("gpu")) gpuAddress = k;
            if(v.equals("screen")) screenAddress = k;
            if(v.equals("eeprom")) eepromAddress = k;
        }
        if(gpuAddress != null && screenAddress != null) {
            machine.invoke(gpuAddress, "bind", new Object[]{screenAddress});
        }
        if(gpuAddress != null) {
            machine.invoke(gpuAddress, "setForeground", new Object[]{0xFFFFFF, false});
            machine.invoke(gpuAddress, "setBackground", new Object[]{0x000000, false});
            Object[] gpuSizeO = machine.invoke(gpuAddress, "getResolution",
                    new Object[]{});
            int w = 40;
            int h = 16;
            if(gpuSizeO.length >= 1 && gpuSizeO[0] instanceof Integer)
                w = (Integer)gpuSizeO[0];
            if(gpuSizeO.length >= 2 && gpuSizeO[1] instanceof Integer)
                h = (Integer)gpuSizeO[1];
            machine.invoke(gpuAddress, "fill", new Object[]{1, 1, w, h, " "});
            machine.invoke(gpuAddress, "set", new Object[]{1, h, "BOOT TEST STRING"});
        }
        if(eepromAddress == null) {
            bsod("ERR_NO_EEPROM");
            return;
        }
        Object[] eepromData = machine.invoke(eepromAddress, "get", new Object[0]);
        if(eepromData != null && eepromData.length > 0 && eepromData[0] instanceof byte[]) {
            IntBuffer data = ByteBuffer.wrap((byte[])eepromData[0])
                    .order(ByteOrder.BIG_ENDIAN)
                    .asIntBuffer();
            int[] program = new int[data.remaining()];
            data.get(program);
            if(ramWords < program.length) {
                bsod("ERR_NO_MEM");
                return;
            }
            System.arraycopy(program, 0, getRAM(), 0, program.length);
        }
    }

    private void bsod(String msg) {
        try {
            BSOD.draw(machine, gpuAddress, msg);
        } catch(Exception e) {
            log("Failed to BSOD: " + e);
            e.printStackTrace();
        }
        crashed = true;
    }

    private int[] getRAM() {
        int[] ram = cpu.getRAM();
        if(ram == null && ramWords > 0) {
            ram = new int[ramWords];
            cpu.setRAM(ram);
        }
        if(ram == null) throw new IllegalStateException("Getting ram before it's available!");
        return ram;
    }

    private static void log(String msg) {
        LogManager.getLogger("OCMIPS").log(Level.DEBUG, msg);
    }
}
