package org.example.illusion.mixin;

import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.example.illusion.IllusionClient;
import org.example.illusion.event.MoveEntityEvent;
import org.example.illusion.event.SendMessageEvent;
import org.example.illusion.event.SprintEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalClientPlayerEntity.class)
public abstract class LocalClientPlayerEntityMixin extends Entity {
    @Unique
    private SendMessageEvent illusion$sendMessage;

    public LocalClientPlayerEntityMixin(World world) {
        super(world);
    }

    @Override
    public void move(double x, double y, double z) {
        MoveEntityEvent event = new MoveEntityEvent(x, y, z);
        IllusionClient.BUS.publish(event);
        if (!event.cancelled) super.move(event.x, event.y, event.z);
    }

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void illusion$sendMessage(String message, CallbackInfo ci) {
        IllusionClient.BUS.publish(illusion$sendMessage = new SendMessageEvent(message));
        if (illusion$sendMessage.cancelled) ci.cancel();
    }

    @ModifyVariable(method = "sendChat", at = @At("HEAD"), argsOnly = true)
    private String illusion$message(String message) {
        return illusion$sendMessage.message;
    }

    @Redirect(method = "sendMovementToServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/living/player/LocalClientPlayerEntity;isSprinting()Z", ordinal = 0))
    private boolean illusion$sprint(LocalClientPlayerEntity player) {
        SprintEvent event = new SprintEvent(player.isSprinting());
        IllusionClient.BUS.publish(event);
        return event.sprinting;
    }
}
