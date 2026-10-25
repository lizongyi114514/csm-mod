package com.example.csm.client;

import com.example.csm.Csm;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 每个客户端玩家挂一层 playeranimator 动画层（优先级 1000，会盖住原版动作）。
 * 动作文件：assets/csm/player_animation/<name>.json
 */
public final class CsmPlayerAnimations {

    private static final Map<UUID, ModifierLayer<IAnimation>> LAYERS = new HashMap<>();

    private CsmPlayerAnimations() {
    }

    private static ModifierLayer<IAnimation> layer(AbstractClientPlayer player) {
        return LAYERS.computeIfAbsent(player.getUUID(), id -> {
            ModifierLayer<IAnimation> layer = new ModifierLayer<>();
            PlayerAnimationAccess.getPlayerAnimLayer(player).addAnimLayer(1000, layer);
            return layer;
        });
    }

    public static void play(int entityId, String animation) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Entity entity = minecraft.level.getEntity(entityId);
        if (!(entity instanceof AbstractClientPlayer player)) {
            return;
        }
        play(player, animation);
    }

    public static void play(AbstractClientPlayer player, String animation) {
        KeyframeAnimation data = PlayerAnimationRegistry.getAnimation(new ResourceLocation(Csm.MODID, animation));
        if (data == null) {
            Csm.LOGGER.debug("[CSM] 找不到玩家动作 csm:{}", animation);
            return;
        }
        layer(player).setAnimation(new KeyframeAnimationPlayer(data));
    }

    public static void forget(UUID uuid) {
        LAYERS.remove(uuid);
    }
}
