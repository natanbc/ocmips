package com.github.natanbc.ocmips.handlers.internal;

import li.cil.oc.api.machine.Machine;

public class MachineInvokeRenderer extends FramebufferRenderer {
    //don't allocate as much data (since the video player spams the framebuffer quite a bit)
    private static final ThreadLocal<Object[]> SINGLE_ELEMENT = ThreadLocal.withInitial(() -> new Object[1]);
    private static final ThreadLocal<Object[]> THREE_ELEMENTS = ThreadLocal.withInitial(() -> new Object[3]);

    private final Machine machine;
    private final String address;
    private final int width;
    private final int height;
    private int currentColor = -1;

    public MachineInvokeRenderer(Machine machine, String address, int width, int height) {
        this.machine = machine;
        this.address = address;
        this.width = width;
        this.height = height;
    }

    @Override
    public void set(int x, int y, int color) throws Exception {
        background(color);
        Object[] o = THREE_ELEMENTS.get();
        o[0] = x + 1;
        o[1] = y + 1;
        o[2] = " ";
        machine.invoke(address, "set", o);
    }

    @Override
    public void clear() throws Exception {
        background(0x000000);
        machine.invoke(address, "fill", new Object[] { 1, 1, width, height, " " });
    }

    private void background(int color) throws Exception {
        if(currentColor != color) {
            Object[] o = SINGLE_ELEMENT.get();
            o[0] = color;
            machine.invoke(address, "setBackground", o);
            currentColor = color;
        }
    }
}
