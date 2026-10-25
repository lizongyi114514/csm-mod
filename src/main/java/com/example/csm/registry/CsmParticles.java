package com.example.csm.registry;

import com.example.csm.Csm;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 三个自定义粒子：血、火花、爆炸环。贴图在 assets/csm/textures/particle/。 */
public final class CsmParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Csm.MODID);

    public static final RegistryObject<SimpleParticleType> BLOOD_SPARK =
            PARTICLES.register("blood_spark", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> CHAIN_SPARK =
            PARTICLES.register("chain_spark", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> BLAST_RING =
            PARTICLES.register("blast_ring", () -> new SimpleParticleType(false));

    private CsmParticles() {
    }
}
