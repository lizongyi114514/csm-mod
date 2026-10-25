package com.example.csm.registry;

import com.example.csm.Csm;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** 眼罩的护甲材质（只为能戴在头上，防御/耐久都很低）。 */
public final class CsmArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> MATERIALS =
            DeferredRegister.create(ForgeRegistries.ARMOR_MATERIALS, Csm.MODID);

    public static final RegistryObject<ArmorMaterial> EYEPATCH = MATERIALS.register("eyepatch", () -> new ArmorMaterial() {
        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
            return 55;
        }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return 1;
        }

        @Override
        public int getEnchantmentValue() {
            return 9;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_LEATHER;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return "csm_eyepatch";
        }

        @Override
        public float getToughness() {
            return 0.0F;
        }

        @Override
        public float getKnockbackResistance() {
            return 0.0F;
        }
    });

    private CsmArmorMaterials() {
    }
}
