package com.github.natanbc.ocmips;

import li.cil.oc.api.API;
import li.cil.oc.api.Machine;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "ocmips", name = "OCMIPS",
        version = "1.0",
        dependencies = "required-after:opencomputers@[1.7.0,)"
)
public class OCMips {
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent ev) {
        System.out.println(API.machine);
        if(API.machine == null) {
            System.out.println("MACHINE IS NULL!!!!!");
            Runtime.getRuntime().exit(0);
        }
        Machine.add(MipsArchitecture.class);
    }
}
