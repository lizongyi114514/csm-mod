package com.example.csm.client;

import com.example.csm.Csm;
import com.example.csm.entity.FoxDevilEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FoxDevilEntityGeoModel extends GeoModel<FoxDevilEntity> {

    private static final ResourceLocation MODEL = new ResourceLocation(Csm.MODID, "geo/fox_devil.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Csm.MODID, "textures/entity/fox_devil.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Csm.MODID, "animations/fox_devil.animation.json");

    @Override
    public ResourceLocation getModelResource(FoxDevilEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FoxDevilEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FoxDevilEntity animatable) {
        return ANIMATION;
    }
}
