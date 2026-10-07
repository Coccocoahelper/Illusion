package org.example.illusion.event;

public final class Render3DEvent implements Event {
    public final float partialTicks;

    public Render3DEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}
