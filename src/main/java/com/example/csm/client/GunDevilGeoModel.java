package com.example.csm.client;

import com.example.csm.Csm;
import com.example.csm.entity.GunDevilEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GunDevilGeoModel extends GeoModel<GunDevilEntity> {

    private static final ResourceLocation MODEL = new ResourceLocation(Csm.MODID, "geo/gun_devil.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Csm.MODID, "textures/entity/gun_devil.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Csm.MODID, "animations/gun_devil.animation.json");

    @Override
    public ResourceLocation getModelResource(GunDevilEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(GunDevilEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(GunDevilEntity animatable) {
        return ANIMATION;
    }
}
