package com.max.magnetized.menu;

import com.max.magnetized.Magnetized;
import com.max.magnetized.block.entity.ElectromagnetBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, Magnetized.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ElectromagnetMenu>> ELECTROMAGNET_MENU =
            MENU_TYPES.register("electromagnet_menu", () ->
                    IMenuTypeExtension.create((containerId, inventory, data) -> {
                        BlockPos pos = data.readBlockPos();
                        ElectromagnetBlockEntity be = (ElectromagnetBlockEntity) inventory.player.level().getBlockEntity(pos);
                        return new ElectromagnetMenu(containerId, be);
                    })
            );
}