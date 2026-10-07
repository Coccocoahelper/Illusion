package org.example.illusion.event;

public final class KeyPressEvent implements Event {
    public final int key;
    public final boolean down;

    public KeyPressEvent(int key, boolean down) {
        this.key = key;
        this.down = down;
    }
}
