package com.max.magnetized;

import com.max.magnetized.menu.ModMenuTypes;
import com.max.magnetized.screen.ElectromagnetScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Magnetized.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ELECTROMAGNET_MENU.get(), ElectromagnetScreen::new);
    }
}