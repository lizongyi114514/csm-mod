package com.example.csm.registry;

import com.example.csm.Csm;
import com.example.csm.entity.FoxDevilEntity;
import com.example.csm.entity.FutureDevilEntity;
import com.example.csm.entity.GunDevilEntity;
import com.example.csm.entity.PochitaEntity;
import com.example.csm.entity.TomatoDevilEntity;
import com.example.csm.entity.KatanaManEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CsmEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Csm.MODID);

    public static final RegistryObject<EntityType<KatanaManEntity>> KATANA_MAN = ENTITIES.register("katana_man",
            () -> EntityType.Builder.of(KatanaManEntity::new, MobCategory.MONSTER)
                    .sized(0.62F, 1.95F)
                    .clientTrackingRange(10)
                    .build(Csm.MODID + ":katana_man"));

    public static final RegistryObject<EntityType<GunDevilEntity>> GUN_DEVIL = ENTITIES.register("gun_devil",
            () -> EntityType.Builder.of(GunDevilEntity::new, MobCategory.MONSTER)
                    .sized(1.4F, 3.6F)
                    .clientTrackingRange(12)
                    .build(Csm.MODID + ":gun_devil"));

    public static final RegistryObject<EntityType<FoxDevilEntity>> FOX_DEVIL = ENTITIES.register("fox_devil",
            () -> EntityType.Builder.of(FoxDevilEntity::new, MobCategory.MONSTER)
                    .sized(2.6F, 2.6F)
                    .clientTrackingRange(12)
                    .build(Csm.MODID + ":fox_devil"));

    public static final RegistryObject<EntityType<FutureDevilEntity>> FUTURE_DEVIL = ENTITIES.register("future_devil",
            () -> EntityType.Builder.of(FutureDevilEntity::new, MobCategory.MONSTER)
                    .sized(1.2F, 2.2F)
                    .clientTrackingRange(10)
                    .build(Csm.MODID + ":future_devil"));

    public static final RegistryObject<EntityType<PochitaEntity>> POCHITA = ENTITIES.register("pochita",
            () -> EntityType.Builder.of(PochitaEntity::new, MobCategory.CREATURE)
                    .sized(0.7F, 0.9F)
                    .clientTrackingRange(10)
                    .build(Csm.MODID + ":pochita"));

    public static final RegistryObject<EntityType<TomatoDevilEntity>> TOMATO_DEVIL = ENTITIES.register("tomato_devil",
            () -> EntityType.Builder.of(TomatoDevilEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 1.2F)
                    .clientTrackingRange(10)
                    .build(Csm.MODID + ":tomato_devil"));

    private CsmEntities() {
    }
}
