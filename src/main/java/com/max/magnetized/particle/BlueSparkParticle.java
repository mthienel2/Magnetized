package com.max.magnetized.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class BlueSparkParticle extends SingleQuadParticle {

    private final SpriteSet sprites;

    protected BlueSparkParticle(ClientLevel level, double x, double y, double z,
                                double xd, double yd, double zd, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.setColor(0.25f, 0.65f, 1.0f);
        this.setAlpha(1.0f);
        this.lifetime = 8 + this.random.nextInt(10);
        this.gravity = 0.04f;
        this.friction = 0.8f;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize = 0.08f + this.random.nextFloat() * 0.06f;
    }

    @Override
    public void tick() {
        super.tick();
        this.setAlpha(1.0f - (float) this.age / (float) this.lifetime);
        this.setSpriteFromAge(this.sprites);
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
            return new BlueSparkParticle(level, x, y, z, xd, yd, zd, sprites);
        }
    }
}
