package com.max.magnetized.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class FieldSparkParticle extends SingleQuadParticle {

    // Fixed (not randomized) so the spawner can compute an exact velocity that
    // covers the electromagnet's configured range within this many ticks.
    public static final int LIFETIME_TICKS = 20;

    private static final float START_SIZE = 0.15f;
    private static final float END_SIZE = 0.45f;

    protected FieldSparkParticle(ClientLevel level, double x, double y, double z,
                                 double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.setColor(0.65f, 0.3f, 1.0f);
        this.setAlpha(1.0f);
        this.lifetime = LIFETIME_TICKS;
        // No gravity/friction so the ring travels in a straight line along the beam
        this.gravity = 0.0f;
        this.friction = 1.0f;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize = START_SIZE;
    }

    @Override
    public void tick() {
        super.tick();
        float progress = (float) this.age / (float) this.lifetime;
        // Ring expands as it travels down the beam, like a tractor-beam pulse
        this.quadSize = START_SIZE + (END_SIZE - START_SIZE) * progress;
        this.setAlpha(1.0f - progress);
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xd, double yd, double zd,
                                       RandomSource random) {
            return new FieldSparkParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
