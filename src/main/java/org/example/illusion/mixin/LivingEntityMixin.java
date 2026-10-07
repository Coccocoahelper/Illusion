package org.example.illusion.mixin;

import net.minecraft.entity.living.LivingEntity;
import org.example.illusion.IllusionClient;
import org.example.illusion.event.JumpDelayEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @ModifyConstant(method = "mobTick", constant = @Constant(intValue = 10))
    private int illusion$jumpDelay(int ticks) {
        JumpDelayEvent event = new JumpDelayEvent();
        IllusionClient.BUS.publish(event);
        return event.ticks;
    }
}
