package com.example.csm.entity;

import com.example.csm.fx.SkillFx;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.joml.Vector3f;

import java.util.EnumSet;

/**
 * 三段节奏：
 *   WINDUP（可读前摇，锁方向） -> STRIKE（居合 / 三连突进 / 刀风） -> RECOVER（惩罚窗口）
 * 前摇长度由 phase 决定，玩家的"未来视"就是拿来吃这个窗口的。
 */
public class KatanaManAttackGoal extends Goal {

    private static final Vector3f BLADE = new Vector3f(0.78F, 0.80F, 0.86F);

    private final KatanaManEntity boss;
    private int stage;      // 0 前摇 / 2 硬直 / 3 连突进
    private int timer;
    private int dashesLeft;

    public KatanaManAttackGoal(KatanaManEntity boss) {
        this.boss = boss;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.boss.canAttack();
    }

    @Override
    public void start() {
        this.stage = 0;
        this.timer = 0;
        this.boss.beginAttack();
        this.boss.getNavigation().stop();
        LivingEntity target = this.boss.getTarget();
        if (target != null) {
            this.boss.getLookControl().setLookAt(target, 60.0F, 60.0F);
        }
        this.boss.level().playSound(null, this.boss.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON,
                SoundSource.HOSTILE, 1.2F, 0.7F);
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.boss.getTarget();
        return this.stage >= 0 && target != null && target.isAlive();
    }

    @Override
    public void tick() {
        LivingEntity target = this.boss.getTarget();
        if (target == null) {
            this.stage = -1;
            return;
        }
        this.boss.getLookControl().setLookAt(target, 60.0F, 60.0F);

        switch (this.stage) {
            case 0 -> {
                this.timer++;
                if (this.boss.level() instanceof ServerLevel server && this.timer % 4 == 0) {
                    SkillFx.gather(server, this.boss.getEyePosition().add(this.boss.getLookAngle().scale(0.9D)), BLADE, 4);
                }
                if (this.timer >= this.boss.windupTicks()) {
                    strike();
                }
            }
            case 2 -> {
                this.timer++;
                if (this.timer >= this.boss.recoveryTicks()) {
                    this.stage = -1;
                }
            }
            case 3 -> {
                this.timer++;
                if (this.timer % 10 == 1) {
                    this.boss.dash();
                    this.dashesLeft--;
                    if (this.dashesLeft <= 0) {
                        this.stage = 2;
                        this.timer = 0;
                    }
                }
            }
            default -> this.stage = -1;
        }
    }

    private void strike() {
        if (this.boss.nextHeavy) {
            this.boss.heavySlash();
            this.stage = 2;
            this.timer = 0;
            return;
        }
        switch (this.boss.phase()) {
            case 1 -> {
                this.boss.slash();
                this.stage = 2;
                this.timer = 0;
            }
            case 2 -> {
                this.dashesLeft = 3;
                this.stage = 3;
                this.timer = 0;
            }
            default -> {
                this.boss.slash();
                this.boss.wave();
                this.stage = 2;
                this.timer = 0;
            }
        }
    }

    @Override
    public void stop() {
        this.boss.setAttacking(false);
        this.boss.startCooldown();
        this.stage = -1;
    }
}
