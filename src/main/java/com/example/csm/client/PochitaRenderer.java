package com.example.csm.client;

import com.example.csm.entity.PochitaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PochitaRenderer extends GeoEntityRenderer<PochitaEntity> {

    public PochitaRenderer(EntityRendererProvider.Context context) {
        super(context, new PochitaGeoModel());
        this.shadowRadius = 0.4F;
    }
}
