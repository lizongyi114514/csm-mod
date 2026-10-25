package com.example.csm.client;

import com.example.csm.Csm;
import com.example.csm.entity.KatanaManEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KatanaManGeoModel extends GeoModel<KatanaManEntity> {

    private static final ResourceLocation MODEL = new ResourceLocation(Csm.MODID, "geo/katana_man.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Csm.MODID, "textures/entity/katana_man.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Csm.MODID, "animations/katana_man.animation.json");

    @Override
    public ResourceLocation getModelResource(KatanaManEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(KatanaManEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(KatanaManEntity animatable) {
        return ANIMATION;
    }
}
