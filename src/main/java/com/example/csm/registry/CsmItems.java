package com.example.csm.registry;

import com.example.csm.Csm;
import com.example.csm.item.ChainsawItem;
import com.example.csm.entity.PochitaEntity;
import com.example.csm.item.EyepatchItem;
import com.example.csm.item.PochitaHeartItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
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

    public static final RegistryObject<Item> TOMATO = ITEMS.register("tomato",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationMod(0.3F).build())));

    public static final RegistryObject<Item> BREAD = ITEMS.register("bread",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationMod(0.7F).build())));

    public static final RegistryObject<Item> STRONGEST_BREAD = ITEMS.register("strongest_bread",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(10).saturationMod(1.2F).alwaysEat()
                    .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 120, 1), 1.0F)
                    .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0), 1.0F)
                    .build())));

    public static final RegistryObject<Item> CHAINSAW_HELMET = ITEMS.register("chainsaw_helmet",
            () -> new ArmorItem(CsmArmorMaterials.CHAINSAW, ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> CHAINSAW_CHESTPLATE = ITEMS.register("chainsaw_chestplate",
            () -> new ArmorItem(CsmArmorMaterials.CHAINSAW, ArmorItem.Type.CHESTPLATE, new Item.Properties()));

    public static final RegistryObject<Item> CHAINSAW_LEGGINGS = ITEMS.register("chainsaw_leggings",
            () -> new ArmorItem(CsmArmorMaterials.CHAINSAW, ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final RegistryObject<Item> CHAINSAW_BOOTS = ITEMS.register("chainsaw_boots",
            () -> new ArmorItem(CsmArmorMaterials.CHAINSAW, ArmorItem.Type.BOOTS, new Item.Properties()));

    public static final RegistryObject<Item> TOMATO_SPAWN_EGG = ITEMS.register("tomato_spawn_egg",
            () -> new ForgeSpawnEggItem(CsmEntities.TOMATO_DEVIL, 0xC62A2A, 0x4A8C3A, new Item.Properties()));

    private CsmItems() {
    }
}
