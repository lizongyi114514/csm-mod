package com.example.csm.item;

import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 电锯物品：GeckoLib 模型 + 常驻 spin 动画。
 * "锯链在转"由 animations/chainsaw.animation.json 里 chain_top / chain_bottom 的往复位移实现。
 */
public class ChainsawItem extends Item implements GeoItem {

    private static final RawAnimation SPIN = RawAnimation.begin().thenLoop("spin");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ChainsawItem(Properties properties) {
        super(properties);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "spin", 0, state -> state.setAndContinue(SPIN)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
