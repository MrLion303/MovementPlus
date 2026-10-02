package com.negativestudios.movementplus.forge;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MovementPlusForge.MOD_ID)
public class MovementPlusForge {
    public static final String MOD_ID="movementplus";
    public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,MOD_ID);
    public static final RegistryObject<Item> LASHING_POTATO=ITEMS.register("lashing_potato",
        ()->new LashingPotatoItem(new Item.Properties().stacksTo(1).durability(100)));
    public MovementPlusForge(){
        IEventBus bus=FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
    }
}
