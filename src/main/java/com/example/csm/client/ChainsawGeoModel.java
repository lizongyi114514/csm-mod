package com.example.csm.client;

import com.example.csm.Csm;
import com.example.csm.item.ChainsawItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ChainsawGeoModel extends GeoModel<ChainsawItem> {

    private static final ResourceLocation MODEL = new ResourceLocation(Csm.MODID, "geo/chainsaw.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Csm.MODID, "textures/item/chainsaw.png");
    private static final ResourceLocation ANIMATION = new ResourceLocation(Csm.MODID, "animations/chainsaw.animation.json");

    @Override
    public ResourceLocation getModelResource(ChainsawItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ChainsawItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ChainsawItem animatable) {
        return ANIMATION;
    }
}
