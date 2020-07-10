package com.github.natanbc.ocmips;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.MipsException;
import com.github.natanbc.mipscpu.MipsRegisters;
import com.github.natanbc.mipscpu.instruction.MipsInstruction;
import com.github.natanbc.mipscpu.memory.MemoryHandler;
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
        cpu = new MipsCPU(OCMips.BOOTROM);
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
    public void load(NBTTagCompound tag) {}

    @Override
    public void save(NBTTagCompound tag) {}

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

    private static void log(String msg) {
        LogManager.getLogger("OCMIPS").log(Level.DEBUG, msg);
    }
}
