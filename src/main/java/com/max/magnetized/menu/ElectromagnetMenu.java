package com.max.magnetized.menu;

import com.max.magnetized.block.entity.ElectromagnetBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class ElectromagnetMenu extends AbstractContainerMenu {

    private final ElectromagnetBlockEntity blockEntity;

    public ElectromagnetMenu(int containerId, ElectromagnetBlockEntity blockEntity) {
        super(ModMenuTypes.ELECTROMAGNET_MENU.get(), containerId);
        this.blockEntity = blockEntity;
    }

    public ElectromagnetBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}