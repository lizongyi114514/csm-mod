package com.example.csm.client;

import com.example.csm.Csm;
import com.example.csm.entity.PochitaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PochitaGeoModel extends GeoModel<PochitaEntity> {

    private static final ResourceLocation MODEL = new ResourceLocation(Csm.MODID, "geo/pochita.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Csm.MODID, "textures/entity/pochita.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Csm.MODID, "animations/pochita.animation.json");

    @Override
    public ResourceLocation getModelResource(PochitaEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(PochitaEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(PochitaEntity animatable) {
        return ANIMATION;
    }
}
