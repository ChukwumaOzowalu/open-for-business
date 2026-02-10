package com.hiipower.openforbusiness.client;

import com.hiipower.openforbusiness.ModMenus;
import com.hiipower.openforbusiness.OpenForBusiness;
import com.hiipower.openforbusiness.client.screen.DisplayShelfScreen;
import com.hiipower.openforbusiness.client.screen.LedgerScreen;
import com.hiipower.openforbusiness.client.screen.RegisterScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = OpenForBusiness.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.REGISTER_MENU.get(), RegisterScreen::new);
            MenuScreens.register(ModMenus.LEDGER_MENU.get(), LedgerScreen::new);
            MenuScreens.register(ModMenus.DISPLAY_SHELF_MENU.get(), DisplayShelfScreen::new);
        });
    }
}
