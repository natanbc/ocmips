package com.github.natanbc.ocmips.utils;

import li.cil.oc.api.machine.Machine;

public class BSOD {
    public static void draw(Machine machine, String gpu, String msg) throws Exception {
        msg = "0x" + Integer.toHexString(msg.hashCode()) + ": " + msg;
        if (gpu != null) {
            Integer maxDepth = (Integer)machine.invoke(gpu, "maxDepth", new Object[0])[0];
            machine.invoke(gpu, "setDepth", new Object[] { maxDepth });
            machine.invoke(gpu, "setForeground", new Object[]{0xFFFFFF, false});
            if(maxDepth == 1) {
                machine.invoke(gpu, "setBackground", new Object[]{0x000000, false});
            } else {
                machine.invoke(gpu, "setBackground", new Object[]{0x0000FF, false});
            }
            Object[] gpuSizeO = machine.invoke(gpu, "maxResolution",
                    new Object[]{});
            int w = 40;
            int h = 16;
            if (gpuSizeO != null && gpuSizeO.length >= 1 && gpuSizeO[0] instanceof Integer)
                w = (Integer) gpuSizeO[0];
            if (gpuSizeO != null && gpuSizeO.length >= 2 && gpuSizeO[1] instanceof Integer)
                h = (Integer) gpuSizeO[1];
            if(w == 160 && h == 50) {
                w = 80;
                h = 25;
                machine.invoke(gpu, "setResolution", new Object[] { w, h });
            }
            machine.invoke(gpu, "fill", new Object[]{1, 1, w, h, " "});
            int y = h / 2 - 5;
            drawSadFace(machine, gpu, 3, 3);
            drawString(machine, gpu, w, y++, "Your PC ran into a problem");
            drawString(machine, gpu, w, y++, "and needs to restart.");
            drawString(machine, gpu, w, y++, "We're just collecting some");
            drawString(machine, gpu, w, y++, "error info and then we'll");
            drawString(machine, gpu, w, y++, "maybe restart for you.");
        
            drawString(machine, gpu, w, y + 2, "0% complete");
            drawString(machine, gpu, w, y + 5, msg);
        }
    }
    
    private static void drawString(Machine machine, String gpu, int w, int y, String s) throws Exception {
        machine.invoke(gpu, "set", new Object[] {
                w / 2 + 1 - s.length() / 2, y, s
        });
    }
    
    private static void drawSadFace(Machine machine, String gpu, int x, int y) throws Exception {
        drawSquare(machine, gpu, x+7, y-1);
        drawSquare(machine, gpu, x+6, y);
        drawSquare(machine, gpu, x, y);
        drawSquare(machine, gpu, x+5, y+1);
        drawSquare(machine, gpu, x+4, y+2);
        drawSquare(machine, gpu, x+5, y+3);
        drawSquare(machine, gpu, x, y+4);
        drawSquare(machine, gpu, x+6, y+4);
        drawSquare(machine, gpu, x+7, y+5);
    }
    
    private static void drawSquare(Machine machine, String gpu, int x, int y) throws Exception {
        Object oldBg = machine.invoke(gpu, "setBackground", new Object[] { 0xFFFFFF })[0];
        machine.invoke(gpu, "set", new Object[] { x,     y, " " });
        machine.invoke(gpu, "set", new Object[] { x + 1, y, " " });
        machine.invoke(gpu, "setBackground", new Object[] { oldBg });
    }
}
