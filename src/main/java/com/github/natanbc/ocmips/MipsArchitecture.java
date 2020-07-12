package com.github.natanbc.ocmips;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.MipsException;
import com.github.natanbc.mipscpu.MipsRegisters;
import com.github.natanbc.mipscpu.instruction.MipsInstruction;
import com.github.natanbc.mipscpu.memory.MemoryHandler;
import com.github.natanbc.ocmips.handlers.BadAppleHandler;
import com.github.natanbc.ocmips.handlers.CleanableHandler;
import com.github.natanbc.ocmips.handlers.ComponentCallHandler;
import com.github.natanbc.ocmips.utils.BSOD;
import com.github.natanbc.ocmips.utils.ConversionHelpers;
import com.github.natanbc.ocmips.utils.HandlerSerialization;
import com.github.natanbc.ocmips.utils.MemoryUtils;
import com.github.natanbc.ocmips.utils.SpecialComponentMapper;
import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.item.Memory;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.machine.Architecture;
import li.cil.oc.api.machine.ExecutionResult;
import li.cil.oc.api.machine.Signal;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Architecture.Name("MIPS")
@Architecture.NoMemoryRequirements
public class MipsArchitecture implements Architecture {
    private static final int MAX_STEPS_PER_CALL = 1500;

    private final Machine machine;
    private int ramWords;
    private MipsCPU cpu;
    private volatile ExecutionResult queuedResult;
    private boolean crashed;

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
        cpu = createCPU(null);
        crashed = false;
        return true;
    }

    @Override
    public void close() {
        cpu = null;
        crashed = false;
    }

    @Override
    public void runSynchronized() {
        updateRam();
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
        try {
            for(int i = 0; i < MAX_STEPS_PER_CALL; i++) {
                cpu.step();
            }
            return new ExecutionResult.Sleep(10);
        } catch (MipsException e) {
            e.printStackTrace(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            try {
                int pc;
                System.out.printf("PC=   0x%x\n", pc = cpu.registers().readInteger(MipsRegisters.PC));
                int instr;
                System.out.printf("Instr=0x%x\n", instr = cpu.readWord(pc));
                System.out.printf("Instr=%s\n", MipsInstruction.decode(instr));
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
    public void load(NBTTagCompound tag) {
        if(tag.getBoolean("mips_powered_on")) {
            crashed = tag.getBoolean("crashed");
            cpu = createCPU(tag.getIntArray("mips_boot_rom"));
            cpu.setRAM(tag.getIntArray("mips_ram"));
            int[] regs = tag.getIntArray("mips_registers");
            for(int i = 0; i < regs.length; i++) {
                cpu.registers().writeInteger(i, regs[i]);
            }
            HandlerSerialization.deserializeHandlers(machine, cpu, tag);
        }
    }

    @Override
    public void save(NBTTagCompound tag) {
        if(cpu != null) {
            tag.setBoolean("mips_powered_on", true);
            tag.setBoolean("crashed", crashed);
            tag.setIntArray("mips_boot_rom", OCMips.BOOTROM);
            tag.setIntArray("mips_ram", cpu.getRAM());
            int[] regs = new int[MipsRegisters.INTEGER_COUNT];
            for(int i = 0; i < regs.length; i++) regs[i] = cpu.registers().readInteger(i);
            tag.setIntArray("mips_registers", regs);
            HandlerSerialization.serializeHandlers(cpu, tag);
        } else {
            tag.setBoolean("mips_powered_on", false);
        }
    }

    private void updateRam() {
        if(cpu != null) {
            if(cpu.getRAM() == null) {
                cpu.setRAM(new int[ramWords]);
            } else if(cpu.getRAM().length != ramWords) {
                int[] n = new int[ramWords];
                System.arraycopy(cpu.getRAM(), 0, n, 0, Math.min(n.length, cpu.getRAM().length));
                cpu.setRAM(n);
            }
        }
    }

    private void bsod(String gpuAddress, String msg) {
        try {
            BSOD.draw(machine, gpuAddress, msg);
        } catch(Exception e) {
            log("Failed to BSOD: " + e);
            queuedResult = new ExecutionResult.Error(msg);
        }
        crashed = true;
    }

    private MipsCPU createCPU(int[] bootrom) {
        if(bootrom == null) bootrom = OCMips.BOOTROM;
        MipsCPU c = new MipsCPU(bootrom);
        c.addMemoryHandler(0x13370000, new BadAppleHandler());
        c.setSyscallHandler(cpu -> {
            switch (cpu.registers().readInteger(MipsRegisters.V0)) {
                //sleep
                //$a0 has the time to sleep
                //returns nothing
                case 1: throw new StopExecution(new ExecutionResult.Sleep(
                        cpu.registers().readInteger(MipsRegisters.A0)
                ));
                //shutdown
                //$a0 is whether or not to reboot. anything other than 0 is a reboot
                //never returns
                case 2: throw new StopExecution(new ExecutionResult.Shutdown(
                        cpu.registers().readInteger(MipsRegisters.A0) != 0
                ));
                //map special component
                //$a0 has the memory address to map into
                //$a1 has the component address
                //$a2 has the type of map to create
                //$a3 has extra flags for the map
                //returns 0 on success
                case 3: {
                    SpecialComponentMapper.map(machine, cpu);
                    return;
                }
                //unmap region
                //$a0 has the address of the memory map to remove
                //returns 0 on success, -1 on failure
                case 4: {
                    int addr = cpu.registers().readInteger(MipsRegisters.A0);
                    MemoryHandler m = cpu.removeMemoryHandler(addr);
                    cpu.registers().writeInteger(MipsRegisters.V0, m == null ? -1 : 0);
                    if(m == null) return;
                    if(m instanceof CleanableHandler) {
                        ((CleanableHandler)m).cleanup(cpu);
                    }
                    return;
                }
                //map component call
                //$a0 has the address of the component_method_t struct
                //$a1 has the address of the component uuid
                //$a2 has the name of the method (eg "beep")
                //$a3 has the maximum number of arguments that might be passed
                //always succeeds
                case 5: {
                    int addr = cpu.registers().readInteger(MipsRegisters.A0);
                    String component = MemoryUtils.readAddress(cpu, cpu.registers().readInteger(MipsRegisters.A1));
                    String method = MemoryUtils.readString(cpu, cpu.registers().readInteger(MipsRegisters.A2));
                    int maxArg = cpu.registers().readInteger(MipsRegisters.A3);
                    cpu.addMemoryHandler(addr, new ComponentCallHandler(machine, component, method, maxArg));
                    cpu.registers().writeInteger(MipsRegisters.V0, 0);
                    return;
                }
                //find Nth component by type
                //$a0 has a string with the component type (eg "gpu")
                //$a1 has which component to get (sorted by address, 0 is the first)
                //$a2 has the memory address to write the component uuid, if found
                //returns 0 if found, 1 if not found
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
                        IntBuffer buffer = ConversionHelpers.toBuffer(UUID.fromString(component.get()));
                        for(int i = 0; i < buffer.capacity(); i++) {
                            cpu.writeWord(addr + i * 4, buffer.get(i));
                        }
                        cpu.registers().writeInteger(MipsRegisters.V0, 0);
                    } else {
                        cpu.registers().writeInteger(MipsRegisters.V0, 1);
                    }
                    return;
                }
                //bsod
                //$a0 has the gpu address to draw the bsod on. may be null
                //$a1 has the bsod message
                //never returns
                case 7: {
                    String addr = MemoryUtils.readAddress(cpu, cpu.registers().readInteger(MipsRegisters.A0));
                    String msg = MemoryUtils.readString(cpu, cpu.registers().readInteger(MipsRegisters.A1));
                    if(addr == null) {
                        queuedResult = new ExecutionResult.Error(msg);
                    } else {
                        bsod(addr, msg);
                    }
                    throw new RetryInNextTick();
                }
                //popSignal
                //$a0 has retval buffer address
                //$a1 has retval buffer size
                //returns the number of arguments of the signal or -1 if no signal was queued
                case 8: {
                    Signal s = machine.popSignal();
                    if(s == null) {
                        cpu.registers().writeInteger(MipsRegisters.V0, -1);
                    } else {
                        Object[] array = new Object[s.args().length + 1];
                        System.arraycopy(s.args(), 0, array, 1, s.args().length);
                        array[0] = s.name();
                        cpu.registers().writeInteger(MipsRegisters.V0, ConversionHelpers.writeObjectsToMemory(
                                cpu, array, cpu.registers().readInteger(MipsRegisters.A0),
                                cpu.registers().readInteger(MipsRegisters.A1)
                        ));
                    }
                    return;
                }
                //who needs gdb when i can have my shitty pseudo printf
                //void rt_dbg(const char* msg, int type, int arg);
                case 9: {
                    String msg = MemoryUtils.readString(cpu, cpu.registers().readInteger(MipsRegisters.A0));
                    int type = cpu.registers().readInteger(MipsRegisters.A1);
                    int val = cpu.registers().readInteger(MipsRegisters.A2);
                    if(type == ConversionHelpers.TYPE_STRING) {
                        System.out.printf("[dbg] %s: %s\n", msg, MemoryUtils.readString(cpu, val));
                    } else if(type == ConversionHelpers.TYPE_ADDRESS) {
                        System.out.printf("[dbg] %s: %s\n", msg, MemoryUtils.readAddress(cpu, val));
                    } else {
                        System.out.printf("[dbg] %s: %d (0x%x)\n", msg, val, val);
                    }
                }
                default: cpu.registers().writeInteger(MipsRegisters.V0, -1);
            }
        });
        return c;
    }

    private static void log(String msg) {
        LogManager.getLogger("OCMIPS").log(Level.DEBUG, msg);
    }
}
