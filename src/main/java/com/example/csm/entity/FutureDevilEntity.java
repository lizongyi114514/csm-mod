package com.example.csm.entity;

import com.example.csm.network.ForesightPacket;
import com.example.csm.registry.CsmEffects;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

/** 未来恶魔：契约对象，浮在契约者身后，持续给「未来视」。 */
public class FutureDevilEntity extends Monster implements GeoEntity {

    private static final EntityDataAccessor<Boolean> CASTING =
            SynchedEntityData.defineId(FutureDevilEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID owner;
    private int life = 300;
    private int castTicks;

    public FutureDevilEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setPersistenceRequired();
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CASTING, false);
    }

    public boolean isCasting() {
        return this.entityData.get(CASTING);
    }

    private void setCasting(boolean value) {
        this.entityData.set(CASTING, value);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5,
                state -> state.setAndContinue(RawAnimation.begin().thenLoop("idle"))));
        controllers.add(new AnimationController<>(this, "cast", 2,
                state -> isCasting() ? state.setAndContinue(RawAnimation.begin().thenPlay("cast")) : PlayState.STOP));
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
        if (this.castTicks > 0) {
            this.castTicks--;
            if (this.castTicks == 0) {
                this.setCasting(false);
            }
        }
        if (--this.life <= 0) {
            vanish();
            return;
        }
        if (this.owner == null) {
            return;
        }
        var player = this.level().getPlayerByUUID(this.owner);
        if (player == null) {
            return;
        }
        Vec3 behind = player.position().add(0.0D, 2.0D, 0.0D)
                .subtract(player.getLookAngle().scale(1.6D));
        Vec3 delta = behind.subtract(position()).scale(0.14D);
        this.setDeltaMovement(getDeltaMovement().scale(0.65D).add(delta));
        this.getLookControl().setLookAt(player, 20.0F, 20.0F);

        if (this.tickCount % 40 == 0 && distanceTo(player) < 14.0D) {
            player.addEffect(new MobEffectInstance(CsmEffects.FORESIGHT.get(), 140, 0, true, false));
            this.setCasting(true);
            this.castTicks = 20;
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.6D, getZ(), 12, 0.4D, 0.4D, 0.4D, 0.01D);
            }
            predictAround(player);
        }
    }

    private void vanish() {
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1.2D, getZ(), 30, 0.6D, 1.0D, 0.6D, 0.1D);
            server.playSound(null, blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.2F, 0.6F);
        }
        this.discard();
    }

    public static FutureDevilEntity summon(ServerPlayer player, int lifeTicks) {
        if (!(player.level() instanceof ServerLevel level)) {
            return null;
        }
        FutureDevilEntity devil = CsmEntities.FUTURE_DEVIL.get().create(level);
        if (devil == null) {
            return null;
        }
        Vec3 pos = player.position().subtract(player.getLookAngle().scale(1.6D)).add(0.0D, 2.0D, 0.0D);
        devil.moveTo(pos.x, pos.y, pos.z, player.getYRot(), 0.0F);
        devil.setOwner(player);
        devil.life = lifeTicks;
        level.addFreshEntity(devil);
        level.sendParticles(ParticleTypes.PORTAL, pos.x, pos.y + 1.0D, pos.z, 30, 0.6D, 0.8D, 0.6D, 0.05D);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.2F);
        return devil;
    }

    /** 预测未来 3 秒：附近敌对生物的攻击力总和，并在它们「将要到达的位置」画残影。 */
    private void predictAround(net.minecraft.world.entity.player.Player player) {
        float damage = 0.0F;
        int threats = 0;
        for (Monster mob : this.level().getEntitiesOfClass(Monster.class, getBoundingBox().inflate(14.0D),
                e -> e != this && e.isAlive())) {
            var attribute = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            float power = attribute == null ? 2.0F : (float) attribute.getValue();
            damage += power * 0.5F;
            threats++;
            if (this.level() instanceof ServerLevel server) {
                Vec3 future = mob.position().add(mob.getDeltaMovement().scale(20.0D));
                double fy = future.y + mob.getBbHeight() * 0.5D;
                server.sendParticles(ParticleTypes.SOUL, future.x, fy, future.z, 10, 0.25D, 0.25D, 0.25D, 0.01D);
                server.sendParticles(ParticleTypes.CRIT, future.x, fy, future.z, 6, 0.2D, 0.2D, 0.2D, 0.05D);
            }
        }
        if (player instanceof ServerPlayer serverPlayer) {
            ForesightPacket.send(serverPlayer, 60, damage, threats);
        }
    }
}
