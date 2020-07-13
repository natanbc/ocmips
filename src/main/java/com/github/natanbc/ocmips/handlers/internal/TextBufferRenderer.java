package com.github.natanbc.ocmips.handlers.internal;

import li.cil.oc.api.internal.TextBuffer;

public class TextBufferRenderer extends FramebufferRenderer {
    private final TextBuffer buffer;
    private int currentColor = -1;

    public TextBufferRenderer(TextBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public void set(int x, int y, int color) {
        if(color != currentColor) {
            buffer.setBackgroundColor(color);
            currentColor = color;
        }
        buffer.set(x + 1, y + 1, " ", false);
    }

    @Override
    public void clear() {
        currentColor = 0x000000;
        buffer.setBackgroundColor(currentColor);
        buffer.fill(1, 1, buffer.getWidth(), buffer.getHeight(), ' ');
    }
}
