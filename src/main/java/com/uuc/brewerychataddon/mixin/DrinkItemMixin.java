package com.uuc.brewerychataddon.mixin;

import com.uuc.brewerychataddon.BreweryChatAddonMod;
import com.uuc.brewerychataddon.BreweryReflection;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "eu.pb4.brewery.item.DrinkItem")
public abstract class DrinkItemMixin {
    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void brewerychataddon$finishUsing(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (world == null || world.isClient()) {
            return;
        }
        if (!(user instanceof ServerPlayerEntity player)) {
            return;
        }

        double quality = BreweryReflection.getDrinkQuality(stack);
        if (quality < 0) {
            return;
        }

        BreweryChatAddonMod.drunkTracker().setLastDrinkQuality(player.getUuid(), quality);
    }
}
