package com.example.csm.registry;

import com.example.csm.Csm;
import com.example.csm.entity.GunDevilEntity;
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

    private CsmEntities() {
    }
}
