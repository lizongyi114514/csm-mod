package com.example.csm.client;

import com.example.csm.entity.TomatoDevilEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TomatoDevilRenderer extends GeoEntityRenderer<TomatoDevilEntity> {

    public TomatoDevilRenderer(EntityRendererProvider.Context context) {
        super(context, new TomatoDevilGeoModel());
        this.shadowRadius = 0.6F;
    }
}
