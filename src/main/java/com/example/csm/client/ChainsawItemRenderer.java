package com.example.csm.client;

import com.example.csm.item.ChainsawItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ChainsawItemRenderer extends GeoItemRenderer<ChainsawItem> {

    public ChainsawItemRenderer() {
        super(new ChainsawGeoModel());
    }
}
