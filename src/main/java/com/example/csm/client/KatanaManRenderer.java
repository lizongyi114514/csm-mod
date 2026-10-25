package com.example.csm.client;

import com.example.csm.entity.KatanaManEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KatanaManRenderer extends GeoEntityRenderer<KatanaManEntity> {

    public KatanaManRenderer(EntityRendererProvider.Context context) {
        super(context, new KatanaManGeoModel());
        this.shadowRadius = 0.5F;
    }
}
