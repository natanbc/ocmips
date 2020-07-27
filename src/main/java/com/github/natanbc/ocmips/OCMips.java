package com.github.natanbc.ocmips;

import com.github.natanbc.ocmips.utils.MemoryUtils;
import li.cil.oc.api.FileSystem;
import li.cil.oc.api.Items;
import li.cil.oc.api.Machine;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.commons.compress.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

@Mod(modid = OCMips.MOD_ID, name = "OCMIPS",
        version = "1.0",
        dependencies = "required-after:opencomputers@[1.7.0,)"
)
public class OCMips {
    public static final String MOD_ID = "ocmips";

    static int[] BOOTROM;

    public static InputStream openResource(String name) {
        InputStream is = OCMips.class.getClassLoader().getResourceAsStream("assets/" + MOD_ID + "/" + name);
        if(is == null) {
            throw new IllegalArgumentException(name + " not found");
        }
        return is;
    }
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent ev) throws IOException {
        Machine.add(MipsArchitecture.class);
        try(InputStream is = openResource("bootrom.bin")) {
            byte[] rom = IOUtils.toByteArray(is);
            IntBuffer data = ((ByteBuffer)
                    MemoryUtils.allocateRounding(rom.length)
                            .put(rom)
                            .position(0))
                    .asIntBuffer();
            BOOTROM = new int[data.remaining()];
            data.get(BOOTROM);
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent ev) throws IOException {
        try(InputStream is = openResource("bios.bin")) {
            ItemStack bios = Items.registerEEPROM("EEPROM (MIPS Bios)",
                    IOUtils.toByteArray(is), null, false);
            GameRegistry.addShapelessRecipe(
                    new ResourceLocation(MOD_ID, "bios"),
                    null,
                    bios,
                    Ingredient.fromStacks(Items.get("eeprom").createItemStack(1)),
                    Ingredient.fromItem(net.minecraft.init.Items.REDSTONE)
            );
        }
        GameRegistry.addShapelessRecipe(
                new ResourceLocation(MOD_ID, "flashhex"),
                null,
                Items.registerFloppy("flashhex", EnumDyeColor.CYAN,
                        () -> FileSystem.fromClass(OCMips.class, MOD_ID, "flash"),
                        true),
                Ingredient.fromStacks(Items.get("floppy").createItemStack(1)),
                Ingredient.fromItem(net.minecraft.init.Items.REDSTONE)
        );
    }
}
