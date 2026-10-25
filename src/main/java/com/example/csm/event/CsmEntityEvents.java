package com.example.csm.event;

import com.example.csm.Csm;
import com.example.csm.entity.FoxDevilEntity;
import com.example.csm.entity.FutureDevilEntity;
import com.example.csm.entity.GunDevilEntity;
import com.example.csm.entity.PochitaEntity;
import com.example.csm.entity.TomatoDevilEntity;
import com.example.csm.entity.KatanaManEntity;
import com.example.csm.registry.CsmEntities;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Csm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CsmEntityEvents {

    private CsmEntityEvents() {
    }

    @SubscribeEvent
    public static void onSpawnPlacement(SpawnPlacementRegisterEvent event) {
        event.register(CsmEntities.TOMATO_DEVIL.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> level.getBlockState(pos.below()).isSolid(),
                SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(CsmEntities.POCHITA.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, spawnType, pos, random) -> level.getBlockState(pos.below()).isSolid(),
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void onEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(CsmEntities.KATANA_MAN.get(), KatanaManEntity.createAttributes().build());
        event.put(CsmEntities.GUN_DEVIL.get(), GunDevilEntity.createAttributes().build());
        event.put(CsmEntities.FOX_DEVIL.get(), FoxDevilEntity.createAttributes().build());
        event.put(CsmEntities.FUTURE_DEVIL.get(), FutureDevilEntity.createAttributes().build());
        event.put(CsmEntities.POCHITA.get(), PochitaEntity.createAttributes().build());
        event.put(CsmEntities.TOMATO_DEVIL.get(), TomatoDevilEntity.createAttributes().build());
    }
}
