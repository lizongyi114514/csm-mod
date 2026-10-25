package com.example.csm.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * 只做两件事：锁定落点（前摇，可躲）→ 开火（换弹由实体自己计时）。
 * 阶段 3 的落点在前摇开始时固定，所以"走出去"就能躲——这是给玩家读的。
 */
public class GunDevilAttackGoal extends Goal {

    private final GunDevilEntity boss;
    private int timer;
    private boolean fired;

    public GunDevilAttackGoal(GunDevilEntity boss) {
        this.boss = boss;
        setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.boss.canAttack();
    }

    @Override
    public void start() {
        this.timer = 0;
        this.fired = false;
        this.boss.setAttacking(true);
        LivingEntity target = this.boss.getTarget();
        if (target != null) {
            this.boss.getLookControl().setLookAt(target, 60.0F, 60.0F);
            this.boss.lockTarget();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !this.fired && this.boss.getTarget() != null;
    }

    @Override
    public void tick() {
        this.timer++;
        if (this.boss.level() instanceof ServerLevel server && this.timer % 3 == 0) {
            this.boss.telegraph(server);
        }
        if (this.timer >= this.boss.windupTicks()) {
            this.boss.fire();
            this.fired = true;
            this.boss.setAttacking(false);
        }
    }

    @Override
    public void stop() {
        this.timer = 0;
        this.boss.setAttacking(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
