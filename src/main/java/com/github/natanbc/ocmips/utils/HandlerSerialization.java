package com.github.natanbc.ocmips.utils;

import com.github.natanbc.mipscpu.MipsCPU;
import com.github.natanbc.mipscpu.memory.MemoryHandler;
import com.github.natanbc.ocmips.handlers.BadAppleHandler;
import com.github.natanbc.ocmips.handlers.ComponentCallHandler;
import com.github.natanbc.ocmips.handlers.DriveHandler;
import com.github.natanbc.ocmips.handlers.EEPROMHandler;
import com.github.natanbc.ocmips.handlers.FramebufferHandler;
import com.github.natanbc.ocmips.handlers.RemapHandler;
import li.cil.oc.api.machine.Machine;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class HandlerSerialization {
    public static void serializeHandlers(MipsCPU cpu, NBTTagCompound tag) {
        NBTTagList l = new NBTTagList();

        cpu.memoryHandlers().forEach((address, handler) -> {
            NBTTagCompound h = new NBTTagCompound();
            h.setInteger("cpu_addr", address);
            serialize(handler, h);
            l.appendTag(h);
        });

        tag.setTag("mips_handlers", l);
    }

    public static void deserializeHandlers(Machine machine, MipsCPU cpu, NBTTagCompound tag) {
        NBTTagList l = tag.getTagList("mips_handlers", Constants.NBT.TAG_COMPOUND);
        l.forEach(h -> {
            NBTTagCompound c = (NBTTagCompound)h;
            cpu.addMemoryHandler(c.getInteger("cpu_addr"), deserialize(machine, c));
        });
    }

    private static void serialize(MemoryHandler handler, NBTTagCompound tag) {
        if(handler instanceof ComponentCallHandler) {
            ComponentCallHandler cch = (ComponentCallHandler)handler;
            tag.setString("type", "component_call");
            tag.setString("address", cch.getComponentAddress());
            tag.setString("method", cch.getMethod());
            tag.setInteger("maxArg", cch.getMaxArg());
            cch.save(tag);
            return;
        }
        if(handler instanceof EEPROMHandler) {
            EEPROMHandler e = (EEPROMHandler)handler;
            tag.setString("type", "eeprom");
            tag.setString("address", e.getEepromAddress());
            tag.setInteger("size", e.getSize());
            tag.setInteger("dataSize", e.getDataSize());
            e.save(tag);
            return;
        }
        if(handler instanceof FramebufferHandler) {
            FramebufferHandler fb = (FramebufferHandler)handler;
            tag.setString("type", "framebuffer");
            tag.setString("address", fb.getGpuAddress());
            tag.setInteger("width", fb.getWidth());
            tag.setInteger("height", fb.getHeight());
            tag.setInteger("bufferNumber", fb.getBufferNumber());
            return;
        }
        if(handler instanceof DriveHandler) {
            DriveHandler h = (DriveHandler)handler;
            tag.setString("type", "drive");
            tag.setString("address", h.getDriveAddress());
            tag.setInteger("mode", h.getMode());
            tag.setInteger("size", h.getSize());
            tag.setInteger("sectorSize", h.getSectorSize());
            h.save(tag);
            return;
        }
        if(handler instanceof RemapHandler) {
            RemapHandler h = (RemapHandler)handler;
            tag.setString("type", "remap");
            tag.setInteger("target", h.getTarget());
            tag.setInteger("size", h.getSize());
            return;
        }
        if(handler instanceof BadAppleHandler) {
            tag.setString("type", "bad_apple");
            return;
        }
        throw new AssertionError();
    }

    private static MemoryHandler deserialize(Machine machine, NBTTagCompound tag) {
        switch (tag.getString("type")) {
            case "component_call": {
                ComponentCallHandler cch = new ComponentCallHandler(
                        machine, tag.getString("address"),
                        tag.getString("method"), tag.getInteger("maxArg")
                );
                cch.restore(tag);
                return cch;
            }
            case "eeprom": {
                EEPROMHandler h = new EEPROMHandler(
                        machine, tag.getString("address"),
                        tag.getInteger("size"), tag.getInteger("dataSize")
                );
                h.restore(tag);
                return h;
            }
            case "framebuffer": {
                return new FramebufferHandler(
                        machine, tag.getString("address"),
                        tag.getInteger("width"), tag.getInteger("height"),
                        tag.getInteger("bufferNumber")
                );
            }
            case "drive": {
                DriveHandler h = new DriveHandler(
                        machine, tag.getString("address"), tag.getInteger("mode"),
                        tag.getInteger("size"), tag.getInteger("sectorSize")
                );
                h.restore(tag);
                return h;
            }
            case "remap": {
                return new RemapHandler(
                        tag.getInteger("target"), tag.getInteger("size")
                );
            }
            case "bad_apple": {
                return new BadAppleHandler();
            }
            default: throw new AssertionError();
        }
    }
}
