package com.example.csm.entity;

import com.example.csm.registry.CsmEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

/** 狐狸恶魔：被狐狸契约召唤出来的巨大狐首，只咬敌对生物，几十秒后消失。 */
public class FoxDevilEntity extends Monster implements GeoEntity {

    private static final EntityDataAccessor<Boolean> BITING =
            SynchedEntityData.defineId(FoxDevilEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID owner;
    private int life = 160;
    private int biteCooldown = 15;
    private int biteTicks;

    public FoxDevilEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setPersistenceRequired();
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BITING, false);
    }

    public boolean isBiting() {
        return this.entityData.get(BITING);
    }

    private void setBiting(boolean value) {
        this.entityData.set(BITING, value);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5,
                state -> state.setAndContinue(RawAnimation.begin().thenLoop("idle"))));
        controllers.add(new AnimationController<>(this, "attack", 2,
                state -> isBiting() ? state.setAndContinue(RawAnimation.begin().thenPlay("bite")) : PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public void setOwner(ServerPlayer player) {
        this.owner = player.getUUID();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (this.biteTicks > 0) {
            this.biteTicks--;
            if (this.biteTicks == 0) {
                this.setBiting(false);
            }
        }
        if (this.biteCooldown > 0) {
            this.biteCooldown--;
        }
        if (--this.life <= 0) {
            vanish();
            return;
        }

        LivingEntity target = findTarget();
        if (target != null) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (this.biteCooldown <= 0 && distanceTo(target) < 11.0D) {
                bite(target);
            }
        } else if (this.owner != null) {
            var player = this.level().getPlayerByUUID(this.owner);
            if (player != null) {
                this.getLookControl().setLookAt(player, 10.0F, 10.0F);
            }
        }
    }

    private LivingEntity findTarget() {
        return this.level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(16.0D),
                        e -> e != this && e.isAlive() && !(e instanceof FoxDevilEntity) && !(e instanceof FutureDevilEntity)
                                && (e instanceof Monster) && !(e instanceof net.minecraft.world.entity.player.Player))
                .stream().findFirst().orElse(null);
    }

    private void bite(LivingEntity target) {
        this.biteCooldown = 50;
        this.biteTicks = 14;
        this.setBiting(true);
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        Vec3 eye = getEyePosition();
        Vec3 dir = target.position().subtract(eye).normalize();
        AABB box = new AABB(eye, eye.add(dir.scale(7.0D))).inflate(2.6D);
        for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && e.isAlive() && !(e instanceof net.minecraft.world.entity.player.Player))) {
            entity.hurt(damageSources().mobAttack(this), 14.0F);
            entity.knockback(0.9D, -dir.x, -dir.z);
        }
        server.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0D, target.getZ(), 18, 0.5D, 0.5D, 0.5D, 0.15D);
        server.playSound(null, target.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 1.4F, 0.7F);
    }

    private void vanish() {
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1.0D, getZ(), 30, 1.0D, 1.0D, 1.0D, 0.05D);
            server.playSound(null, blockPosition(), SoundEvents.FOX_SCREECH, SoundSource.HOSTILE, 1.2F, 0.6F);
        }
        this.discard();
    }

    // 契约召唤
    public static FoxDevilEntity summon(ServerPlayer player, int lifeTicks) {
        if (!(player.level() instanceof ServerLevel level)) {
            return null;
        }
        FoxDevilEntity fox = CsmEntities.FOX_DEVIL.get().create(level);
        if (fox == null) {
            return null;
        }
        Vec3 pos = player.position().add(player.getLookAngle().scale(3.5D)).add(0.0D, 1.2D, 0.0D);
        fox.moveTo(pos.x, pos.y, pos.z, player.getYRot(), 0.0F);
        fox.setOwner(player);
        fox.life = lifeTicks;
        level.addFreshEntity(fox);
        level.sendParticles(ParticleTypes.SOUL, pos.x, pos.y + 1.0D, pos.z, 40, 1.2D, 1.2D, 1.2D, 0.02D);
        level.playSound(null, player.blockPosition(), SoundEvents.FOX_AMBIENT, SoundSource.PLAYERS, 1.6F, 0.5F);
        return fox;
    }
}
