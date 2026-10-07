package org.example.illusion.event;

import org.example.illusion.feature.module.api.Module;

public final class ModuleToggleEvent implements Event {
    public final Module module;
    public final boolean enabled;

    public ModuleToggleEvent(Module module, boolean enabled) {
        this.module = module;
        this.enabled = enabled;
    }
}
