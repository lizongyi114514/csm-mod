package com.example.csm.entity;

import com.example.csm.fx.SkillFx;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import com.example.csm.registry.CsmItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.UUID;

/**
 * 武士刀男：三阶段读招型 Boss。
 *
 * 阶段 1 居合（<=200）：24 tick 前摇 -> 12 格直线斩 -> 16 tick 硬直（惩罚窗口）
 * 阶段 2 二刀流（<=150）：三次突进，第三次之后 20 tick 硬直
 * 阶段 3 暴走（<=120）：移速 +、前摇缩短到 14 tick，追加刀风（6 格 AOE）
 */
public class KatanaManEntity extends Monster implements GeoEntity {

    private static final UUID FRENZY_SPEED = UUID.fromString("c1d2e3f4-0001-4b02-8c03-000000000001");
    private static final Vector3f BLADE = new Vector3f(0.78F, 0.80F, 0.86F);

    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(KatanaManEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int attackCooldown;
    private boolean frenzied;

    public KatanaManEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACKING, false);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, state ->
                state.setAndContinue(state.isMoving()
                        ? RawAnimation.begin().thenLoop("walk")
                        : RawAnimation.begin().thenLoop("idle"))));
        controllers.add(new AnimationController<>(this, "attack", 2, state ->
                isAttacking() ? state.setAndContinue(RawAnimation.begin().thenPlay("attack")) : PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new KatanaManAttackGoal(this));
        this.goalSelector.addGoal(5, new MoveTowardsTargetGoal(this, 1.0D, 24.0F));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        int phase = desiredPhase();
        if (phase == 3 && !this.frenzied) {
            this.frenzied = true;
            var speed = getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.addTransientModifier(new AttributeModifier(FRENZY_SPEED, "csm_frenzy", 0.12D,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
            announce();
        }
    }

    // ---------------- 阶段 ----------------

    public int phase() {
        return desiredPhase();
    }

    private int desiredPhase() {
        float health = getHealth();
        if (health <= 120.0F) {
            return 3;
        }
        if (health <= 150.0F) {
            return 2;
        }
        return 1;
    }

    private void announce() {
        if (level() instanceof ServerLevel server) {
            server.playSound(null, blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.6F, 0.7F);
            server.sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + 1.0D, getZ(), 8, 1.0D, 0.6D, 1.0D, 0.05D);
        }
        var target = getTarget();
        if (target != null) {
            target.displayClientMessage(net.minecraft.network.chat.Component.translatable("csm.boss.katana.frenzy"), true);
        }
    }

    // ---------------- 攻击节奏 ----------------

    public boolean canAttack() {
        return this.attackCooldown <= 0 && getTarget() != null && getTarget().isAlive();
    }

    public int windupTicks() {
        return switch (phase()) {
            case 1 -> 24;
            case 2 -> 18;
            default -> 14;
        };
    }

    public int recoveryTicks() {
        return switch (phase()) {
            case 1 -> 16;
            case 2 -> 20;
            default -> 10;
        };
    }

    public void startCooldown() {
        this.attackCooldown = switch (phase()) {
            case 1 -> 60;
            case 2 -> 45;
            default -> 30;
        };
    }

    /** 居合：12 格直线，宽度 1.4，伤害 12 */
    public void slash() {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        Vec3 eye = getEyePosition();
        Vec3 dir = getLookAngle();
        AABB box = new AABB(eye, eye.add(dir.scale(12.0D))).inflate(1.6D);

        for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e.isAlive())) {
            Vec3 to = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D).subtract(eye);
            if (to.cross(dir).length() > 1.4D) {
                continue;
            }
            entity.hurt(damageSources().mobAttack(this), 12.0F);
            entity.knockback(1.2D, -dir.x, -dir.z);
        }

        SkillFx.beam(server, eye, dir, 12.0D, BLADE, 16);
        server.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 2.0F, 0.8F);
        server.playSound(null, blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.8F, 1.6F);
    }

    /** 突进：向前冲 6 格，接触伤害 6 */
    public void dash() {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        Vec3 dir = getLookAngle();
        setDeltaMovement(dir.x * 0.95D, 0.15D, dir.z * 0.95D);
        hasImpulse = true;

        AABB box = getBoundingBox().inflate(1.5D).move(dir.x, 0.0D, dir.z);
        for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e.isAlive())) {
            entity.hurt(damageSources().mobAttack(this), 6.0F);
            entity.knockback(0.6D, -dir.x, -dir.z);
        }
        SkillFx.burst(server, position(), BLADE, 6);
    }

    /** 刀风：6 格环形，伤害 8，只出现在阶段 3 */
    public void wave() {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(6.0D),
                e -> e != this && e.isAlive())) {
            entity.hurt(damageSources().mobAttack(this), 8.0F);
        }
        SkillFx.ring(server, position(), 6.0D, BLADE);
        server.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 0.7F, 1.4F);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (this.random.nextFloat() < 0.35F + looting * 0.1F) {
            spawnAtLocation(new ItemStack(CsmItems.CHAINSAW.get()));
        }
    }
}
