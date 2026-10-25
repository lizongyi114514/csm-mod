package com.example.csm.registry;

import com.example.csm.Csm;
import com.example.csm.world.DevilNestFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CsmFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, Csm.MODID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DEVIL_NEST =
            FEATURES.register("devil_nest", () -> new DevilNestFeature(NoneFeatureConfiguration.CODEC));

    private CsmFeatures() {
    }
}
