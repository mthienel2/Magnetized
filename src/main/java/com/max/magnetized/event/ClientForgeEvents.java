package com.max.magnetized.event;

import com.max.magnetized.Magnetized;
import com.max.magnetized.client.ModKeyMappings;
import com.max.magnetized.network.ToggleMagnetPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Magnetized.MODID, value = Dist.CLIENT)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) {
            return;
        }

        while (ModKeyMappings.TOGGLE_MAGNET.consumeClick()) {
            ClientPacketDistributor.sendToServer(new ToggleMagnetPacket());
        }
    }
}