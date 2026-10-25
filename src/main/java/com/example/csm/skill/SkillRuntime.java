package com.example.csm.skill;

import com.example.csm.Csm;
import com.example.csm.config.CsmConfig;
import com.example.csm.damage.CsmDamage;
import com.example.csm.fx.SkillFx;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3f;
import com.example.csm.player.PlayerResources;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 一次施法的运行时。三段状态机：WINDUP -> ACTIVE -> RECOVERY -> DONE。
 * 命中在进入 ACTIVE 的那一帧结算一次（不是每 tick 扫描）。
 */
public final class SkillRuntime {

    public enum Phase {
        WINDUP, ACTIVE, RECOVERY, DONE
    }

    private final ServerPlayer caster;
    private final SkillDefinition definition;
    private Phase phase = Phase.WINDUP;
    private int tick;

    public SkillRuntime(ServerPlayer caster, SkillDefinition definition) {
        this.caster = caster;
        this.definition = definition;
    }

    public SkillDefinition definition() {
        return definition;
    }

    public Phase phase() {
        return phase;
    }

    /** 返回 true 表示这次施法结束 */
    public boolean tick() {
        if (phase == Phase.DONE) {
            return true;
        }
        SkillDefinition.Cast cast = definition.cast();

        switch (phase) {
            case WINDUP -> {
                if (tick % 2 == 0) {
                    SkillFx.windup(caster, definition);
                }
                if (tick >= cast.windup()) {
                    enter(Phase.ACTIVE);
                }
            }
            case ACTIVE -> {
                if (tick >= cast.active()) {
                    enter(Phase.RECOVERY);
                }
            }
            case RECOVERY -> {
                if (tick >= cast.recovery()) {
                    phase = Phase.DONE;
                    return true;
                }
            }
            default -> {
                return true;
            }
        }

        tick++;
        return phase == Phase.DONE;
    }

    private void enter(Phase next) {
        phase = next;
        tick = 0;
        if (next == Phase.ACTIVE) {
            resolveHit();
        }
    }

    private void resolveHit() {
        if (!caster.isAlive()) {
            phase = Phase.DONE;
            return;
        }
        SkillDefinition.Hit hit = definition.hit();
        Vec3 direction = caster.getLookAngle();
        SkillFx.trail(caster, definition);

        for (LivingEntity target : findTargets(hit)) {
            if (!hit.selfOnly() && target instanceof Player other && !CsmConfig.pvp()) {
                continue;
            }
            float damage = (float) (hit.damage() * CsmConfig.damageMultiplier());
            boolean detonate = "detonate".equals(definition.special());
            if (detonate) {
                damage += BombStacks.consume(target) * BombStacks.DAMAGE_PER_STACK;
            }
            if (damage > 0.0F) {
                target.hurt(CsmDamage.technique(caster), damage);
            }
            if (detonate && caster.level() instanceof ServerLevel server) {
                SkillFx.burst(server, target.position(), new Vector3f(1.0F, 0.42F, 0.12F), 18);
            }
            if ("prime".equals(definition.special())) {
                BombStacks.add(target, 1);
            }
            for (SkillDefinition.Effect effect : hit.effects()) {
                applyEffect(target, effect);
            }
            SkillFx.hit(caster, target.position(), definition);
            if (hit.knockback() > 0.0D) {
                // 原版 knockback 会朝传入方向的反方向推，所以这里取负
                target.knockback(hit.knockback(), -direction.x, -direction.z);
            }
        }

        if (hit.damage() > 0.0D) {
            PlayerResources.addEnergy(caster, (int) (hit.damage() * 0.5D));
        }
        playSound();
    }

    private List<LivingEntity> findTargets(SkillDefinition.Hit hit) {
        if (hit.selfOnly()) {
            return List.of(caster);
        }
        Level level = caster.level();
        Vec3 eye = caster.getEyePosition();
        Vec3 direction = caster.getLookAngle();
        double range = Math.max(0.5D, hit.range());

        AABB box = "sphere".equals(hit.shape())
                ? caster.getBoundingBox().inflate(range)
                : new AABB(eye, eye.add(direction.scale(range))).inflate(Math.max(0.6D, hit.width() * 0.5D + 0.6D));

        List<LivingEntity> found = new ArrayList<>();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box, candidate -> candidate != caster && candidate.isAlive())) {
            if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            Vec3 toTarget = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D).subtract(eye);

            if ("cone".equals(hit.shape())) {
                double angle = Math.toDegrees(Math.acos(Mth.clamp(toTarget.normalize().dot(direction), -1.0D, 1.0D)));
                if (angle > hit.angle() * 0.5D) {
                    continue;
                }
            } else if ("ray".equals(hit.shape())) {
                double distanceFromLine = toTarget.cross(direction).length();
                if (distanceFromLine > Math.max(0.6D, hit.width() * 0.5D + 0.6D)) {
                    continue;
                }
            } else if ("single".equals(hit.shape()) && toTarget.length() > range) {
                continue;
            }
            found.add(entity);
        }

        found.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(caster)));
        return found;
    }

    private void applyEffect(LivingEntity target, SkillDefinition.Effect effect) {
        MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(effect.id()));
        if (mobEffect == null) {
            Csm.LOGGER.warn("[CSM] 技能 {} 引用了不存在的效果 {}", definition.id(), effect.id());
            return;
        }
        target.addEffect(new MobEffectInstance(mobEffect, effect.duration(), effect.amplifier(), false, true));
    }

    private void playSound() {
        String soundId = definition.visual().sound();
        if (soundId == null || soundId.isEmpty()) {
            return;
        }
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.tryParse(soundId));
        if (sound == null) {
            return;
        }
        caster.level().playSound(null, caster.blockPosition(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
