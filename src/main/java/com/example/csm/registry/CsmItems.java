package com.example.csm.registry;

import com.example.csm.Csm;
import com.example.csm.item.ChainsawItem;
import com.example.csm.entity.PochitaEntity;
import com.example.csm.item.EyepatchItem;
import com.example.csm.item.PochitaHeartItem;
import net.minecraftforge.common.ForgeSpawnEggItem;
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

    /** 眼罩：戴上封住未来视。 */
    public static final RegistryObject<Item> EYEPATCH = ITEMS.register("eyepatch", EyepatchItem::new);

    /** 波奇塔的心脏：吃掉转电锯形态。 */
    public static final RegistryObject<Item> POCHITA_HEART = ITEMS.register("pochita_heart", PochitaHeartItem::new);

    public static final RegistryObject<Item> POCHITA_SPAWN_EGG = ITEMS.register("pochita_spawn_egg",
            () -> new ForgeSpawnEggItem(CsmEntities.POCHITA, 0xD68436, 0x3B2A1A, new Item.Properties()));

    private CsmItems() {
    }
}
