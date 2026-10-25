package com.example.csm.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.example.csm.Csm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 预知残影：采样附近玩家/敌对生物的位移，按速度外推出 8 / 16 tick 后的位置，
 * 用原版 EntityRenderDispatcher 重绘两遍半透明克隆。参考 JCraft 的 prediction renderer 思路。
 */
@Mod.EventBusSubscriber(modid = Csm.MODID, value = Dist.CLIENT)
public final class ForesightGhosts {

    private static final Map<Integer, double[]> SNAPSHOTS = new HashMap<>();
    private static final double RANGE = 32.0D;

    private ForesightGhosts() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || ClientForesight.ticks() <= 0) {
            SNAPSHOTS.clear();
            return;
        }
        Set<Integer> seen = new HashSet<>();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity == minecraft.player || !entity.isAlive()) {
                continue;
            }
            if (!(entity instanceof Player) && !(entity instanceof Monster)) {
                continue;
            }
            if (entity.distanceToSqr(minecraft.player) > RANGE * RANGE) {
                continue;
            }
            int id = entity.getId();
            seen.add(id);
            double[] previous = SNAPSHOTS.get(id);
            double vx = 0.0D;
            double vy = 0.0D;
            double vz = 0.0D;
            if (previous != null) {
                vx = entity.getX() - previous[0];
                vy = entity.getY() - previous[1];
                vz = entity.getZ() - previous[2];
            }
            SNAPSHOTS.put(id, new double[]{entity.getX(), entity.getY(), entity.getZ(), vx, vy, vz});
        }
        SNAPSHOTS.keySet().removeIf(id -> !seen.contains(id));
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || ClientForesight.ticks() <= 0) {
            return;
        }
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        var dispatcher = minecraft.getEntityRenderDispatcher();
        float partial = event.getPartialTick();
        float fade = Math.min(1.0F, ClientForesight.ticks() / 20.0F);

        for (Map.Entry<Integer, double[]> entry : SNAPSHOTS.entrySet()) {
            Entity entity = minecraft.level.getEntity(entry.getKey());
            if (entity == null || !entity.isAlive()) {
                continue;
            }
            double[] snap = entry.getValue();
            double speed = Math.sqrt(snap[3] * snap[3] + snap[4] * snap[4] + snap[5] * snap[5]);
            if (speed > 2.0D) {
                continue;
            }
            for (int pass = 0; pass < 2; pass++) {
                int lead = pass == 0 ? 8 : 16;
                float alpha = (pass == 0 ? 0.34F : 0.16F) * fade;
                double x = snap[0] + snap[3] * lead - camera.x;
                double y = snap[1] + snap[4] * lead - camera.y;
                double z = snap[2] + snap[5] * lead - camera.z;
                RenderSystem.setShaderColor(1.0F, 0.82F, 0.82F, alpha);
                dispatcher.render(entity, x, y, z, entity.getYRot(), partial, pose, buffers, 0xF000F0);
            }
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        buffers.endBatch();
    }
}
