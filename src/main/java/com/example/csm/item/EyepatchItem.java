package com.example.csm.item;

import com.example.csm.registry.CsmArmorMaterials;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

/** 眼罩：戴上就看不见未来的伤害，未来视会被封住。 */
public class EyepatchItem extends ArmorItem {

    public EyepatchItem() {
        super(CsmArmorMaterials.EYEPATCH.get(), ArmorItem.Type.HELMET, new Item.Properties());
    }
}
