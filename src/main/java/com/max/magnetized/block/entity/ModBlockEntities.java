package com.max.magnetized.block.entity;

import com.max.magnetized.Magnetized;
import com.max.magnetized.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Magnetized.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectromagnetBlockEntity>> ELECTROMAGNET_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("electromagnet_block_entity", () ->
                    new BlockEntityType<>(
                            ElectromagnetBlockEntity::new,
                            false,
                            ModBlocks.ELECTROMAGNET_BLOCK.get()
                    )
            );
}