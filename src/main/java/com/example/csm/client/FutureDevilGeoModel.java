package com.example.csm.client;

import com.example.csm.Csm;
import com.example.csm.entity.FutureDevilEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FutureDevilGeoModel extends GeoModel<FutureDevilEntity> {

    private static final ResourceLocation MODEL = new ResourceLocation(Csm.MODID, "geo/future_devil.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Csm.MODID, "textures/entity/future_devil.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Csm.MODID, "animations/future_devil.animation.json");

    @Override
    public ResourceLocation getModelResource(FutureDevilEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FutureDevilEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FutureDevilEntity animatable) {
        return ANIMATION;
    }
}
