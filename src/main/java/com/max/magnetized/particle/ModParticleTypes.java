package com.max.magnetized.particle;

import com.max.magnetized.Magnetized;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Magnetized.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLUE_SPARK =
            PARTICLE_TYPES.register("blue_spark", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIELD_SPARK =
            PARTICLE_TYPES.register("field_spark", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIELD_SPARK_PULL =
            PARTICLE_TYPES.register("field_spark_pull", () -> new SimpleParticleType(false));
}