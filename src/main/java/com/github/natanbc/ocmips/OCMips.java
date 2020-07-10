package com.github.natanbc.ocmips;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.memory.MemoryHandler;
import com.github.natanbc.mipscpu.memory.MemoryOperationException;
import li.cil.oc.api.Machine;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.compress.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Mod(modid = "ocmips", name = "OCMIPS",
        version = "1.0",
        dependencies = "required-after:opencomputers@[1.7.0,)"
)
public class OCMips {
    private static byte[] badApple;
    private static IntBuffer badAppleBuffer;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent ev) throws IOException {
        Machine.add(MipsArchitecture.class);
        try(InputStream is = getClass().getClassLoader().getResourceAsStream("assets/ocmips/badapple.bin")) {
            if(is == null) throw new AssertionError("badapple.bin not found");
            badApple = IOUtils.toByteArray(is);
            badAppleBuffer = ByteBuffer.wrap(badApple).asIntBuffer();
        }
    }
    
    public static MemoryHandler badApple() {
        if(badApple == null) return null;
        return new MemoryHandler() {
            private int base;
    
            @Override
            public void onAttach(MipsCPU cpu, int baseAddress) {
                this.base = baseAddress;
            }
    
            @Override
            public int read(MipsCPU cpu, int address) {
                return badAppleBuffer.get((address - base) / 4);
            }
    
            @Override
            public void write(MipsCPU cpu, int address, int value) throws MemoryOperationException {
                throw new MemoryOperationException(address, MemoryOperationException.Reason.READ_ONLY);
            }
    
            @Override
            public int memorySize() {
                return badApple.length;
            }
        };
    }
}
