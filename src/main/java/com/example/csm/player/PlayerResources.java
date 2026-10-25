package com.example.csm.player;

import com.example.csm.Csm;
import com.example.csm.form.Form;
import com.example.csm.form.FormManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * 玩家状态统一存在 player.getPersistentData() 的 "csm" 子标签里：
 * energy / form / formTicks / flags / cooldowns。
 *
 * 为什么不用 Capability：这层数据只有本模组读，持久化 + NBT 足够；
 * 等要暴露给别人时再包一层 Capability / 附件。
 */
public final class PlayerResources {

    private static final String ROOT = "csm";
    private static final String ENERGY = "energy";
    private static final String FORM = "form";
    private static final String FORM_TICKS = "formTicks";
    private static final String FLAGS = "flags";
    private static final String COOLDOWNS = "cooldowns";

    private PlayerResources() {
    }

    public static CompoundTag root(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(ROOT, Tag.TAG_COMPOUND)) {
            data.put(ROOT, new CompoundTag());
        }
        return data.getCompound(ROOT);
    }

    // ---------------- energy ----------------

    public static int energy(Player player) {
        return root(player).getInt(ENERGY);
    }

    public static int maxEnergy(Player player) {
        return FormManager.getForm(player).maxEnergy();
    }

    public static void setEnergy(Player player, int value) {
        root(player).putInt(ENERGY, Mth.clamp(value, 0, maxEnergy(player)));
    }

    public static void addEnergy(Player player, int delta) {
        setEnergy(player, energy(player) + delta);
    }

    public static boolean spendEnergy(Player player, int amount) {
        if (energy(player) < amount) {
            return false;
        }
        setEnergy(player, energy(player) - amount);
        return true;
    }

    // ---------------- form ----------------

    public static String form(Player player) {
        return root(player).getString(FORM);
    }

    public static void setForm(Player player, String formId) {
        root(player).putString(FORM, formId);
    }

    public static int formTicks(Player player) {
        return root(player).getInt(FORM_TICKS);
    }

    public static void setFormTicks(Player player, int ticks) {
        root(player).putInt(FORM_TICKS, Math.max(0, ticks));
    }

    // ---------------- flags ----------------

    public static boolean hasFlag(Player player, String flag) {
        if (flag == null || flag.isEmpty()) {
            return true;
        }
        ListTag list = root(player).getList(FLAGS, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            if (list.getString(i).equals(flag)) {
                return true;
            }
        }
        return false;
    }

    public static void setFlag(Player player, String flag, boolean on) {
        ListTag old = root(player).getList(FLAGS, Tag.TAG_STRING);
        ListTag out = new ListTag();
        boolean present = false;
        for (int i = 0; i < old.size(); i++) {
            String value = old.getString(i);
            if (value.equals(flag)) {
                present = true;
                if (on) {
                    out.add(StringTag.valueOf(value));
                }
            } else {
                out.add(StringTag.valueOf(value));
            }
        }
        if (on && !present) {
            out.add(StringTag.valueOf(flag));
        }
        root(player).put(FLAGS, out);
    }

    public static void clearFormFlags(Player player) {
        for (Form form : Form.values()) {
            setFlag(player, form.flag(), false);
        }
    }

    // ---------------- cooldowns ----------------

    public static long cooldownEnd(Player player, ResourceLocation id) {
        CompoundTag map = root(player).getCompound(COOLDOWNS);
        return map.getLong(id.toString());
    }

    public static boolean onCooldown(Player player, ResourceLocation id) {
        return player.level().getGameTime() < cooldownEnd(player, id);
    }

    public static int cooldownRemaining(Player player, ResourceLocation id) {
        return (int) Math.max(0, cooldownEnd(player, id) - player.level().getGameTime());
    }

    public static void setCooldown(Player player, ResourceLocation id, int ticks) {
        if (ticks <= 0) {
            return;
        }
        CompoundTag map = root(player).getCompound(COOLDOWNS);
        map.putLong(id.toString(), player.level().getGameTime() + ticks);
        root(player).put(COOLDOWNS, map);
    }

    // ---------------- misc ----------------

    /** 死亡/换维度时把数据带到新实体 */
    public static void copy(Player from, Player to) {
        to.getPersistentData().put(ROOT, root(from).copy());
        Csm.LOGGER.debug("[CSM] 复制玩家数据 {} -> {}", from.getName().getString(), to.getName().getString());
    }
}
