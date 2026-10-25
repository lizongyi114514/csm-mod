package com.example.csm.client;

import com.example.csm.Csm;
import com.example.csm.registry.CsmEntities;
import com.example.csm.client.particle.CsmSparkParticle;
import com.example.csm.registry.CsmParticles;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 客户端注册：GeckoLib 物品渲染器 + 玩家变身层。 */
@Mod.EventBusSubscriber(modid = Csm.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CsmClientSetup {

    private CsmClientSetup() {
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(CsmParticles.BLOOD_SPARK.get(),
                sprites -> CsmSparkParticle.provider(sprites, 0.62F, 0.07F, 0.07F));
        event.registerSpriteSet(CsmParticles.CHAIN_SPARK.get(),
                sprites -> CsmSparkParticle.provider(sprites, 1.00F, 0.62F, 0.22F));
        event.registerSpriteSet(CsmParticles.BLAST_RING.get(),
                sprites -> CsmSparkParticle.provider(sprites, 1.00F, 0.55F, 0.30F));
    }


    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CsmEntities.KATANA_MAN.get(), KatanaManRenderer::new);
        event.registerEntityRenderer(CsmEntities.GUN_DEVIL.get(), GunDevilRenderer::new);
        event.registerEntityRenderer(CsmEntities.FOX_DEVIL.get(), FoxDevilRenderer::new);
        event.registerEntityRenderer(CsmEntities.FUTURE_DEVIL.get(), FutureDevilRenderer::new);
        event.registerEntityRenderer(CsmEntities.POCHITA.get(), PochitaRenderer::new);
    }

    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new ChainsawFormLayer(renderer));
            }
        }
    }
}
