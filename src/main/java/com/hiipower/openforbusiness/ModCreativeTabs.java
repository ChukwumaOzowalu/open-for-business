package com.hiipower.openforbusiness;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OpenForBusiness.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeTabs {

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Put Register in the Functional Blocks tab
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.REGISTER_ITEM);
            event.accept(ModItems.LEDGER_ITEM.get());
            event.accept(ModItems.DISPLAY_SHELF_ITEM.get());
            event.accept(ModItems.SHOP_SIGN_ITEM.get());
        }
    }
}
