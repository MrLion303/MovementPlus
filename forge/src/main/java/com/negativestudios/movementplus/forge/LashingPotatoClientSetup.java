package com.negativestudios.movementplus.forge;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MovementPlusForge.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class LashingPotatoClientSetup {
    private LashingPotatoClientSetup() {}

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
            MovementPlusForge.LASHING_POTATO.get(),
            new ResourceLocation("minecraft", "lashing_potato_extended"),
            (stack, level, entity, seed) -> LashingPotatoItem.state(stack) != 0 ? 1.0F : 0.0F
        ));
    }
}