package com.max.magnetized.client;

import com.max.magnetized.component.ModDataComponents;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record MagnetActiveProperty() implements ConditionalItemModelProperty {

    public static final MapCodec<MagnetActiveProperty> MAP_CODEC = MapCodec.unit(new MagnetActiveProperty());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
        // Get the MAGNET_ACTIVE data component, defaulting to false if not present
        return stack.getOrDefault(ModDataComponents.MAGNET_ACTIVE.get(), false);
    }

    @Override
    public MapCodec<MagnetActiveProperty> type() {
        return MAP_CODEC;
    }
}
