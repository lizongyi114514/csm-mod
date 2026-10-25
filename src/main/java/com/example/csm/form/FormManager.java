package com.example.csm.form;

import com.example.csm.config.CsmConfig;
import com.example.csm.network.PlayAnimPacket;
import com.example.csm.network.SkillStatePacket;
import com.example.csm.player.PlayerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * 形态切换与计时。开关走 R 键（FormTogglePacket），也可以用 /csm form 指定。
 *
 * 规则（见 GDD 3.1）：
 *  - 人形 -> 电锯：随时
 *  - 电锯 -> 波奇塔：仅当血量 <= 20%
 *  - 任何形态 -> 人形：立即，并附带 5 秒虚弱
 */
public final class FormManager {

    private static final int WEAKNESS_TICKS = 100;

    private FormManager() {
    }

    public static Form getForm(net.minecraft.world.entity.player.Player player) {
        String id = PlayerResources.form(player);
        return id.isEmpty() ? Form.HUNTER : Form.byId(id);
    }

    public static void setForm(ServerPlayer player, Form form) {
        PlayerResources.clearFormFlags(player);
        PlayerResources.setForm(player, form.id());
        PlayerResources.setFlag(player, form.flag(), true);
        PlayerResources.setEnergy(player, Math.min(PlayerResources.energy(player), form.maxEnergy()));
        int duration = form.timed()
                ? (int) Math.round(form.duration() * CsmConfig.formDurationMultiplier())
                : 0;
        PlayerResources.setFormTicks(player, duration);
        if (form.timed()) {
            PlayAnimPacket.send(player, "transform_pull");
        }
        SkillStatePacket.send(player);
    }

    public static void toggle(ServerPlayer player) {
        Form current = getForm(player);
        switch (current) {
            case HUNTER -> setForm(player, Form.DENJI);
            case DENJI -> {
                if (player.getHealth() <= player.getMaxHealth() * 0.2F) {
                    setForm(player, Form.POCHITA);
                } else {
                    revert(player);
                }
            }
            default -> revert(player);
        }
    }

    public static void revert(ServerPlayer player) {
        setForm(player, Form.HUNTER);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, WEAKNESS_TICKS, 1, false, false));
    }

    /** 每 tick：形态计时、能量回复 */
    public static void tick(ServerPlayer player) {
        Form form = getForm(player);
        if (form.timed()) {
            int left = PlayerResources.formTicks(player);
            if (left <= 0) {
                revert(player);
                return;
            }
            PlayerResources.setFormTicks(player, left - 1);
            if (left - 1 <= 0) {
                revert(player);
                return;
            }
        }
        if (player.tickCount % CsmConfig.energyRegenTicks() == 0
                && PlayerResources.energy(player) < PlayerResources.maxEnergy(player)) {
            PlayerResources.addEnergy(player, 1);
        }
    }
}
