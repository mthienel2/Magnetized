package com.max.magnetized.compat;

import com.max.magnetized.component.ModDataComponents;
import com.max.magnetized.item.MagnetItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosCompat {

    public static ItemStack findMagnetInCurios(Player player) {
        return CuriosApi.getCuriosInventory(player).map(handler ->
                handler.getStacksHandler("charm").map(stacksHandler -> {
                    for (int i = 0; i < stacksHandler.getSlots(); i++) {
                        ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                        if (stack.getItem() instanceof MagnetItem) {
                            return stack;
                        }
                    }
                    return ItemStack.EMPTY;
                }).orElse(ItemStack.EMPTY)
        ).orElse(ItemStack.EMPTY);
    }

    public static boolean hasActiveMagnetInCurios(Player player) {
        ItemStack stack = findMagnetInCurios(player);
        if (!stack.isEmpty()) {
            return stack.getOrDefault(ModDataComponents.MAGNET_ACTIVE.get(), false);
        }
        return false;
    }
}