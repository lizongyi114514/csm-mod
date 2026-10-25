package com.example.csm.registry;

import com.example.csm.Csm;
import com.example.csm.item.ChainsawItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CsmItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Csm.MODID);

    /** 电锯：GeckoLib 模型 + spin 动画（chain_top / chain_bottom 沿导板往复） */
    public static final RegistryObject<Item> CHAINSAW = ITEMS.register("chainsaw",
            () -> new ChainsawItem(new Item.Properties().stacksTo(1).durability(500)));

    private CsmItems() {
    }
}
