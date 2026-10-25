package com.example.csm.client;

import com.example.csm.entity.FoxDevilEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FoxDevilEntityRenderer extends GeoEntityRenderer<FoxDevilEntity> {

    public FoxDevilEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new FoxDevilEntityGeoModel());
        this.shadowRadius = 1.2F;
    }
}
