package com.example.csm.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 通用粒子：用一张 8x8 的小贴图 + 颜色/重力/寿命参数，做出血点、火花、爆炸环三种观感。
 */
public class CsmSparkParticle extends TextureSheetParticle {

    private final SpriteSet sprites;

    protected CsmSparkParticle(ClientLevel level, double x, double y, double z,
                               double dx, double dy, double dz, SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.xd = dx * 0.12D;
        this.yd = dy * 0.12D;
        this.zd = dz * 0.12D;
        this.lifetime = 10 + this.random.nextInt(8);
        this.quadSize = 0.10F + this.random.nextFloat() * 0.06F;
        this.gravity = 0.9F;
        this.friction = 0.92F;
        this.setSpriteFromAge(sprites);
    }

    public CsmSparkParticle setTint(float r, float g, float b) {
        this.setColor(r, g, b);
        return this;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.alpha = 1.0F - (float) this.age / this.lifetime;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites, float r, float g, float b) {
        return (type, level, x, y, z, dx, dy, dz) ->
                new CsmSparkParticle(level, x, y, z, dx, dy, dz, sprites).setTint(r, g, b);
    }
}
