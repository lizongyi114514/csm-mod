package com.example.csm.client;

import com.example.csm.entity.FutureDevilEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FutureDevilEntityRenderer extends GeoEntityRenderer<FutureDevilEntity> {

    public FutureDevilEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new FutureDevilEntityGeoModel());
        this.shadowRadius = 0.7F;
    }
}
