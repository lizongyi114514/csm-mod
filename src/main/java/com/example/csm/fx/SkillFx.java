package com.example.csm.fx;

import com.example.csm.config.CsmConfig;
import com.example.csm.form.Form;
import com.example.csm.form.FormManager;
import com.example.csm.registry.CsmParticles;
import com.example.csm.skill.SkillDefinition;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * 三层特效（全部服务端 sendParticles，客户端不用写渲染代码）：
 *   windup  起手：手边聚能
 *   trail   轨迹：沿判定方向/环身采样
 *   hit     命中：目标位置爆点 + 暴击星
 *
 * 颜色按形态区分：混血/魔人=血，玛奇玛=锁链灰，人类=火花橙。
 * 数量受 config 的 particleDensity 影响，整个模块受 particles 开关控制。
 */
public final class SkillFx {

    private static final Vector3f BLOOD = new Vector3f(0.48F, 0.08F, 0.08F);
    private static final Vector3f CHAIN = new Vector3f(0.64F, 0.66F, 0.70F);
    private static final Vector3f SPARK = new Vector3f(1.00F, 0.70F, 0.34F);

    private SkillFx() {
    }

    private static int scaled(int base) {
        return Math.max(1, (int) Math.round(base * CsmConfig.particleDensity()));
    }

    private static Vector3f color(ServerPlayer player) {
        Form form = FormManager.getForm(player);
        return switch (form) {
            case DENJI, POCHITA, POWER -> BLOOD;
            case MAKIMA -> CHAIN;
            default -> SPARK;
        };
    }

    // ---------------- 技能三层 ----------------

    public static void windup(ServerPlayer player, SkillDefinition definition) {
        if (!CsmConfig.particles()) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        Vector3f c = color(player);
        Vec3 hand = player.getEyePosition()
                .add(player.getLookAngle().scale(0.5D))
                .add(0.0D, -0.25D, 0.0D);
        for (int i = 0; i < scaled(3); i++) {
            double ox = (player.getRandom().nextDouble() - 0.5D) * 0.45D;
            double oy = (player.getRandom().nextDouble() - 0.5D) * 0.45D;
            double oz = (player.getRandom().nextDouble() - 0.5D) * 0.45D;
            level.sendParticles(new DustParticleOptions(c, 0.7F),
                    hand.x + ox, hand.y + oy, hand.z + oz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    public static void trail(ServerPlayer player, SkillDefinition definition) {
        if (!CsmConfig.particles()) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        Vector3f c = color(player);
        Vec3 eye = player.getEyePosition();
        Vec3 dir = player.getLookAngle();
        double range = Math.max(2.0D, definition.hit().range());

        if ("sphere".equals(definition.hit().shape()) && !definition.hit().selfOnly()) {
            int points = Math.max(8, scaled(24));
            for (int i = 0; i < points; i++) {
                double angle = Math.PI * 2 * i / points;
                double x = player.getX() + Math.cos(angle) * range * 0.6D;
                double z = player.getZ() + Math.sin(angle) * range * 0.6D;
                level.sendParticles(new DustParticleOptions(c, 0.9F),
                        x, player.getY() + 0.4D, z, 1, 0.0D, 0.05D, 0.0D, 0.0D);
            }
            return;
        }

        if ("sphere".equals(definition.hit().shape())) {
            return;
        }

        int steps = Math.max(4, scaled(14));
        for (int i = 1; i <= steps; i++) {
            Vec3 point = eye.add(dir.scale(range * i / steps));
            level.sendParticles(new DustParticleOptions(c, 0.9F),
                    point.x, point.y, point.z, 2, 0.12D, 0.12D, 0.12D, 0.02D);
        }
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, eye.x + dir.x, eye.y + dir.y - 0.2D, eye.z + dir.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    public static void hit(ServerPlayer player, Vec3 position, SkillDefinition definition) {
        if (!CsmConfig.particles()) {
            return;
        }
        ServerLevel level = (ServerLevel) player.level();
        Vector3f c = color(player);
        level.sendParticles(CsmParticles.BLOOD_SPARK.get(),
                position.x, position.y + 1.0D, position.z, scaled(12), 0.35D, 0.35D, 0.35D, 0.15D);
        level.sendParticles(ParticleTypes.CRIT,
                position.x, position.y + 1.0D, position.z, Math.max(1, scaled(6)), 0.25D, 0.25D, 0.25D, 0.2D);
    }

    // ---------------- Boss 用的通用形状 ----------------

    /** 直线：从起点沿方向撒点（居合、突进、枪击） */
    public static void beam(ServerLevel level, Vec3 from, Vec3 direction, double length, Vector3f color, int steps) {
        if (!CsmConfig.particles()) {
            return;
        }
        int count = Math.max(1, scaled(steps));
        for (int i = 1; i <= count; i++) {
            Vec3 point = from.add(direction.scale(length * i / count));
            level.sendParticles(CsmParticles.CHAIN_SPARK.get(),
                    point.x, point.y, point.z, 2, 0.1D, 0.1D, 0.1D, 0.05D);
        }
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, from.x + direction.x, from.y + direction.y - 0.2D,
                from.z + direction.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    /** 爆点：命中/突进落点 */
    public static void burst(ServerLevel level, Vec3 position, Vector3f color, int count) {
        if (!CsmConfig.particles()) {
            return;
        }
        int total = scaled(count);
        level.sendParticles(CsmParticles.BLOOD_SPARK.get(),
                position.x, position.y + 1.0D, position.z, total, 0.3D, 0.3D, 0.3D, 0.12D);
        level.sendParticles(ParticleTypes.CRIT, position.x, position.y + 1.0D, position.z, Math.max(1, total / 2),
                0.25D, 0.25D, 0.25D, 0.15D);
    }

    /** 环形：刀风、范围技 */
    public static void ring(ServerLevel level, Vec3 center, double radius, Vector3f color) {
        if (!CsmConfig.particles()) {
            return;
        }
        level.sendParticles(CsmParticles.BLAST_RING.get(),
                center.x, center.y + 0.6D, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        int points = Math.max(8, scaled(32));
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2 * i / points;
            level.sendParticles(new DustParticleOptions(color, 1.0F),
                    center.x + Math.cos(angle) * radius, center.y + 0.6D, center.z + Math.sin(angle) * radius,
                    1, 0.0D, 0.05D, 0.0D, 0.0D);
        }
    }

    /** 起手聚能：前摇期间在武器位置聚点 */
    public static void gather(ServerLevel level, Vec3 position, Vector3f color, int count) {
        if (!CsmConfig.particles()) {
            return;
        }
        int total = scaled(count);
        for (int i = 0; i < total; i++) {
            double ox = (level.random.nextDouble() - 0.5D) * 0.5D;
            double oy = (level.random.nextDouble() - 0.5D) * 0.5D;
            double oz = (level.random.nextDouble() - 0.5D) * 0.5D;
            level.sendParticles(new DustParticleOptions(color, 0.6F),
                    position.x + ox, position.y + oy, position.z + oz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }
}
