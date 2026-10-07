package org.example.illusion.event;

import net.minecraft.client.render.Window;

public final class Render2DEvent implements Event {
    public final float partialTicks;
    public final Window window;

    public Render2DEvent(float partialTicks, Window window) {
        this.partialTicks = partialTicks;
        this.window = window;
    }
}
