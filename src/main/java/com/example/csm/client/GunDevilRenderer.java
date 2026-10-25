package com.example.csm.client;

import com.example.csm.entity.GunDevilEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GunDevilRenderer extends GeoEntityRenderer<GunDevilEntity> {

    public GunDevilRenderer(EntityRendererProvider.Context context) {
        super(context, new GunDevilGeoModel());
        this.shadowRadius = 1.4F;
    }
}
