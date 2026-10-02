package com.negativestudios.movementplus.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class MovementPlusFabric implements ModInitializer {
    public static final String MOD_ID = "movementplus";
    public static final Item LASHING_POTATO = Registry.register(
        Registries.ITEM, new Identifier(MOD_ID, "lashing_potato"),
        new LashingPotatoItem(new FabricItemSettings().maxCount(1).maxDamage(100))
    );
    @Override public void onInitialize() {}
}
