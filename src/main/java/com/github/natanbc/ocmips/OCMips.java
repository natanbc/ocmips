package com.github.natanbc.ocmips;

import com.github.natanbc.ocmips.utils.MemoryUtils;
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
    public static IntBuffer badAppleBuffer;
    static int[] BOOTROM;
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent ev) throws IOException {
        Machine.add(MipsArchitecture.class);
        try(InputStream is = getClass().getClassLoader().getResourceAsStream("assets/ocmips/bootrom.bin")) {
            if(is == null) throw new AssertionError("bootrom.bin not found");
            byte[] rom = IOUtils.toByteArray(is);
            IntBuffer data = ((ByteBuffer)
                    MemoryUtils.allocateRounding(rom.length)
                            .put(rom)
                            .position(0))
                    .asIntBuffer();
            BOOTROM = new int[data.remaining()];
            data.get(BOOTROM);
        }
        try(InputStream is = OCMips.class.getClassLoader().getResourceAsStream("assets/ocmips/badapple.bin")) {
            if(is == null) throw new AssertionError("badapple.bin not found");
            byte[] badApple = IOUtils.toByteArray(is);
            ByteBuffer bb = MemoryUtils.allocateRounding(badApple.length);
            bb.put(badApple).position(0);
            badAppleBuffer = bb.asIntBuffer();
        }
    }
}
