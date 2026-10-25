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
import net.minecraft.world.entity.Monster;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
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

/** 波奇塔：橙色小狗，头顶电锯，会跟着主人咬恶魔。 */
public class PochitaEntity extends PathfinderMob implements GeoEntity {

    private static final EntityDataAccessor<Boolean> REVVING =
            SynchedEntityData.defineId(PochitaEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID owner;
    private int attackCooldown;

    public PochitaEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(REVVING, false);
    }

    public boolean isRevving() {
        return this.entityData.get(REVVING);
    }

    private void setRevving(boolean value) {
        this.entityData.set(REVVING, value);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 4, state ->
                state.setAndContinue(state.isMoving()
                        ? RawAnimation.begin().thenLoop("walk")
                        : RawAnimation.begin().thenLoop("idle"))));
        controllers.add(new AnimationController<>(this, "saw", 2, state ->
                isRevving() ? state.setAndContinue(RawAnimation.begin().thenLoop("rev")) : PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                e -> !(e instanceof FoxDevilEntity) && !(e instanceof FutureDevilEntity)));
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
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        boolean revving = getTarget() != null && getTarget().isAlive();
        if (revving != isRevving()) {
            setRevving(revving);
            if (revving && this.level() instanceof ServerLevel server) {
                server.playSound(null, blockPosition(), SoundEvents.MINECART_INSIDE_UNDERWATER, SoundSource.NEUTRAL, 0.6F, 1.6F);
            }
        }
        if (this.owner != null) {
            Player player = this.level().getPlayerByUUID(this.owner);
            if (player != null && distanceToSqr(player) > 36.0D) {
                this.getNavigation().moveTo(player, 1.3D);
            }
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && this.level() instanceof ServerLevel server) {
            Vec3 look = getLookAngle();
            server.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 0.6D, target.getZ(),
                    3, 0.3D, 0.3D, 0.3D, 0.05D);
            server.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL, 1.0F, 1.4F);
            target.push(look.x * 0.4D, 0.25D, look.z * 0.4D);
            this.attackCooldown = 20;
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && source.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker
                && !(attacker instanceof Player)) {
            this.setTarget(attacker);
        }
        return result;
    }

    public static PochitaEntity summon(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return null;
        }
        PochitaEntity dog = CsmEntities.POCHITA.get().create(level);
        if (dog == null) {
            return null;
        }
        Vec3 pos = player.position().add(player.getLookAngle().scale(2.0D));
        dog.moveTo(pos.x, player.getY(), pos.z, player.getYRot(), 0.0F);
        dog.setOwner(player);
        dog.setPersistenceRequired();
        level.addFreshEntity(dog);
        level.sendParticles(ParticleTypes.HEART, pos.x, pos.y + 0.8D, pos.z, 8, 0.4D, 0.4D, 0.4D, 0.02D);
        return dog;
    }
}
