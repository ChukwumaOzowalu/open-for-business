package com.hiipower.openforbusiness;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, OpenForBusiness.MODID);

    public static final RegistryObject<Item> REGISTER_ITEM = ITEMS.register("register",
            () -> new BlockItem(ModBlocks.REGISTER.get(), new Item.Properties()));
    
    public static final RegistryObject<Item> LEDGER_ITEM = ITEMS.register("ledger",
            () -> new BlockItem(ModBlocks.LEDGER.get(), new Item.Properties()));

        public static final RegistryObject<Item> DISPLAY_SHELF_ITEM = ITEMS.register("display_shelf",
                () -> new BlockItem(ModBlocks.DISPLAY_SHELF.get(), new Item.Properties()));



    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
