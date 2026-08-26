package com.max.magnetized.event;

import com.max.magnetized.Magnetized;
import com.max.magnetized.client.MagnetActiveProperty;
import com.max.magnetized.client.ModKeyMappings;
import com.max.magnetized.particle.BlueSparkParticle;
import com.max.magnetized.particle.FieldSparkParticle;
import com.max.magnetized.particle.ModParticleTypes;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = Magnetized.MODID, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void registerConditionalProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(
                Identifier.fromNamespaceAndPath(Magnetized.MODID, "magnet_active"),
                MagnetActiveProperty.MAP_CODEC
        );
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.TOGGLE_MAGNET);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.BLUE_SPARK.get(), BlueSparkParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FIELD_SPARK.get(), FieldSparkParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FIELD_SPARK_PULL.get(), FieldSparkParticle.Provider::new);
    }

}