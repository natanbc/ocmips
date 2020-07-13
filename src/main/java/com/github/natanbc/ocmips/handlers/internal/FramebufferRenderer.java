package com.github.natanbc.ocmips.handlers.internal;

import li.cil.oc.api.internal.TextBuffer;
import li.cil.oc.api.machine.Machine;
import li.cil.oc.api.network.Environment;
import org.apache.logging.log4j.LogManager;
import scala.Option;

import java.lang.reflect.Method;

public abstract class FramebufferRenderer {
    private static final Method GPU_GET_BUFFER;
    private static boolean loggedError;

    static {
        Method m = null;
        try {
            Class<?> gpu = Class.forName("li.cil.oc.server.component.GraphicsCard");
            m = gpu.getMethod("getBuffer", int.class);
        } catch (Exception ignored) {}
        GPU_GET_BUFFER = m;
    }

    FramebufferRenderer() {}

    public abstract void set(int x, int y, int color) throws Exception;

    public abstract void clear() throws Exception;

    public static FramebufferRenderer create(Machine machine, String gpuAddress, int bufferNumber, int width, int height) {
        //if(true) return new MachineInvokeRenderer(machine, gpuAddress, width, height);
        if(GPU_GET_BUFFER == null) {
            return new MachineInvokeRenderer(machine, gpuAddress, width, height);
        }
        Environment gpu = machine.node().network().node(gpuAddress).host();
        try {
            Option<?> opt = (Option<?>)GPU_GET_BUFFER.invoke(gpu, bufferNumber);
            if(!opt.isDefined()) {
                throw new IllegalArgumentException("Attempt to create a framebuffer renderer for invalid buffer number " + bufferNumber);
            }
            return new TextBufferRenderer((TextBuffer)opt.get());
        } catch (ReflectiveOperationException|ClassCastException e) {
            if(!loggedError) {
                loggedError = true;
                LogManager.getLogger("ocmips").error("Failed to create TextBufferRenderer", e);
            }
            return new MachineInvokeRenderer(machine, gpuAddress, width, height);
        }
    }
}
