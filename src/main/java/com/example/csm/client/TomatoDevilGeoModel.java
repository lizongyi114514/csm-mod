package com.example.csm.client;

import com.example.csm.Csm;
import com.example.csm.entity.TomatoDevilEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TomatoDevilGeoModel extends GeoModel<TomatoDevilEntity> {

    private static final ResourceLocation MODEL = new ResourceLocation(Csm.MODID, "geo/tomato_devil.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Csm.MODID, "textures/entity/tomato_devil.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Csm.MODID, "animations/tomato_devil.animation.json");

    @Override
    public ResourceLocation getModelResource(TomatoDevilEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(TomatoDevilEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(TomatoDevilEntity animatable) {
        return ANIMATION;
    }
}
