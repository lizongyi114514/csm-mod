package com.example.csm.client;

import com.example.csm.entity.FutureDevilEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FutureDevilRenderer extends GeoEntityRenderer<FutureDevilEntity> {

    public FutureDevilRenderer(EntityRendererProvider.Context context) {
        super(context, new FutureDevilGeoModel());
        this.shadowRadius = 0.7F;
    }
}
