package com.example.csm;

import com.example.csm.config.CsmClientConfig;
import com.example.csm.config.CsmConfig;
import com.example.csm.network.CsmNetwork;
import com.example.csm.registry.CsmEffects;
import com.example.csm.registry.CsmEntities;
import com.example.csm.registry.CsmItems;
import com.example.csm.registry.CsmFeatures;
import com.example.csm.registry.CsmParticles;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.thread.DistExecutor;
import org.slf4j.Logger;
import net.minecraftforge.api.distmarker.Dist;
import org.slf4j.LoggerFactory;

/**
 * 入口类只做三件事：注册效果、注册网络、把事件类交给 Forge。
 * 事件监听都写在 {@code com.example.csm.event.CommonEvents}（@Mod.EventBusSubscriber）。
 */
@Mod(Csm.MODID)
public final class Csm {
    public static final String MODID = "csm";
    public static final Logger LOGGER = LoggerFactory.getLogger("CSM");

    public Csm() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        CsmEffects.EFFECTS.register(modBus);
        CsmItems.ITEMS.register(modBus);
        CsmEntities.ENTITIES.register(modBus);
        CsmParticles.PARTICLES.register(modBus);
        CsmFeatures.FEATURES.register(modBus);
        CsmNetwork.register();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CsmConfig.SPEC);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> CsmClientConfig::register);
        LOGGER.info("[CSM] Chainsaw Man Mod 初始化完成");
    }
}
