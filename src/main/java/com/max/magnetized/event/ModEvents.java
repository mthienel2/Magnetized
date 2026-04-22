package com.max.magnetized.event;

import com.max.magnetized.Magnetized;
import com.max.magnetized.compat.CuriosCompat;
import com.max.magnetized.component.ModDataComponents;
import com.max.magnetized.item.ModItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = Magnetized.MODID)
public class ModEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (level.isClientSide()) {
            return;
        }

        ItemStack magnetStack = findActiveMagnetInHotbar(player);

        if (magnetStack.isEmpty()) {
            magnetStack = CuriosCompat.findMagnetInCurios(player);
        }

        if (!magnetStack.isEmpty()) {
            boolean active = magnetStack.getOrDefault(ModDataComponents.MAGNET_ACTIVE.get(), false);
            if (active) {
                pullItemsToPlayer(player, level);
            }
        }
    }

    private static ItemStack findActiveMagnetInHotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.is(ModItems.MAGNET_ITEM.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void pullItemsToPlayer(Player player, Level level) {
        int radius = 7;

        AABB area = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);

        for (ItemEntity item : items) {
            double dx = player.getX() - item.getX();
            double dy = (player.getY() + 0.3) - item.getY();
            double dz = player.getZ() - item.getZ();

            dy *= 0.25;

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance > 0.5) {
                double speed = 0.3;

                item.setDeltaMovement(
                        dx / distance * speed,
                        dy / distance * speed,
                        dz / distance * speed
                );
            }
        }
    }
}