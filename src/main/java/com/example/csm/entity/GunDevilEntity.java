package com.example.csm.entity;

import com.example.csm.fx.SkillFx;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
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

/**
 * 枪之恶魔：不追人，只在中远距离压制。
 *
 * 阶段 1（400-241）：单发点射，12 tick 前摇，24 格直线。
 * 阶段 2（240-121）：三连扇形，8 tick 前摇。
 * 阶段 3（<=120）：标记轰炸，40 tick 前摇，落点固定，走出 4 格就能躲。
 * 每次开火后进入换弹（30–50 tick），那是唯一的输出窗口。
 */
public class GunDevilEntity extends Monster implements GeoEntity {

    private static final Vector3f MUZZLE = new Vector3f(0.95F, 0.72F, 0.25F);

    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(GunDevilEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int reloadTicks;
    private Vec3 lockedPos;

    public GunDevilEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 120;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D);
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
        this.goalSelector.addGoal(0, new GunDevilAttackGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 40.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.reloadTicks > 0) {
            this.reloadTicks--;
        }
    }

    public int phase() {
        float health = getHealth();
        if (health <= 120.0F) {
            return 3;
        }
        if (health <= 240.0F) {
            return 2;
        }
        return 1;
    }

    public boolean canAttack() {
        LivingEntity target = getTarget();
        return this.reloadTicks <= 0 && target != null && target.isAlive() && distanceTo(target) < 44.0D;
    }

    public int windupTicks() {
        return switch (phase()) {
            case 1 -> 12;
            case 2 -> 8;
            default -> 40;
        };
    }

    public void lockTarget() {
        LivingEntity target = getTarget();
        if (target != null) {
            this.lockedPos = target.position();
        }
    }

    public void fire() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        Vec3 eye = getEyePosition();
        int phase = phase();

        if (phase == 3 && this.lockedPos != null) {
            explode(server, this.lockedPos);
        } else if (phase == 2) {
            Vec3 base = (this.lockedPos == null ? eye.add(getLookAngle().scale(24.0D)) : this.lockedPos).subtract(eye).normalize();
            for (int offset : new int[]{-12, 0, 12}) {
                beam(server, eye, rotateY(base, offset), 24.0D);
            }
        } else {
            Vec3 to = (this.lockedPos == null ? eye.add(getLookAngle().scale(24.0D)) : this.lockedPos).subtract(eye);
            beam(server, eye, to.normalize(), Math.min(24.0D, to.length() + 2.0D));
        }

        this.reloadTicks = switch (phase) {
            case 1 -> 30;
            case 2 -> 24;
            default -> 50;
        };
        server.playSound(null, blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.HOSTILE, 2.0F, 0.6F);
    }

    private static Vec3 rotateY(Vec3 direction, double degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(direction.x * cos - direction.z * sin, direction.y, direction.x * sin + direction.z * cos);
    }

    private void beam(ServerLevel server, Vec3 from, Vec3 direction, double length) {
        Vec3 to = from.add(direction.scale(length));
        AABB box = new AABB(from, to).inflate(1.5D);
        for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class, box, e -> e != this && e.isAlive())) {
            Vec3 center = entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
            Vec3 rel = center.subtract(from);
            double along = rel.dot(direction);
            if (along < 0.0D || along > length) {
                continue;
            }
            if (rel.subtract(direction.scale(along)).length() > 1.3D) {
                continue;
            }
            entity.hurt(damageSources().mobAttack(this), 6.0F);
        }
        SkillFx.beam(server, from, direction, length, MUZZLE, 24);
        server.sendParticles(ParticleTypes.SMOKE, from.x, from.y, from.z, 6, 0.2D, 0.2D, 0.2D, 0.01D);
    }

    private void explode(ServerLevel server, Vec3 center) {
        for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(4.0D),
                e -> e != this && e.isAlive())) {
            entity.hurt(damageSources().mobAttack(this), 10.0F);
        }
        SkillFx.ring(server, center, 4.0D, MUZZLE);
        SkillFx.burst(server, center, MUZZLE, 24);
        server.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5D, center.z, 3, 1.0D, 0.5D, 1.0D, 0.0D);
        server.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.4F, 0.8F);
    }

    public void telegraph(ServerLevel server) {
        Vec3 eye = getEyePosition();
        for (int i = 0; i < 3; i++) {
            server.sendParticles(new net.minecraft.core.particles.DustParticleOptions(MUZZLE, 0.8F),
                    eye.x, eye.y, eye.z, 1, 0.25D, 0.25D, 0.25D, 0.0D);
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        spawnAtLocation(new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 8 + this.random.nextInt(9)));
    }
}
