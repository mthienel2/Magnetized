package com.max.magnetized.block;

import com.max.magnetized.Magnetized;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Magnetized.MODID);

    public static final DeferredBlock<MagnetNullifierBlock> MAGNET_NULLIFIER = BLOCKS.register(
            "magnet_nullifier",
            registryName -> new MagnetNullifierBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.METAL)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.COPPER)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 4)
            )
    );
}

