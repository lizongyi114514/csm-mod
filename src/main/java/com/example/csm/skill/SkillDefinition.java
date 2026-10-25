package com.example.csm.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 一个招式 = 一份 JSON。字段与 skill-schema/skill.schema.json 对应。
 * 未识别的字段会被 Codec 忽略（例如 trail / once_per_target），方便以后加字段。
 */
public record SkillDefinition(
        ResourceLocation id,
        String school,
        int tier,
        Cost cost,
        Cast cast,
        Hit hit,
        Visual visual,
        Requires requires,
        int slot,
        String special
) {

    /** 没写 slot 的招式只能通过命令释放。必须声明在 CODEC 之前，否则 CODEC 会捕获默认值 0。 */
    public static final int NO_SLOT = 99;

    public static final Codec<SkillDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(SkillDefinition::id),
            Codec.STRING.optionalFieldOf("school", "none").forGetter(SkillDefinition::school),
            Codec.INT.optionalFieldOf("tier", 0).forGetter(SkillDefinition::tier),
            Cost.CODEC.fieldOf("cost").forGetter(SkillDefinition::cost),
            Cast.CODEC.fieldOf("cast").forGetter(SkillDefinition::cast),
            Hit.CODEC.fieldOf("hit").forGetter(SkillDefinition::hit),
            Visual.CODEC.optionalFieldOf("visual", Visual.EMPTY).forGetter(SkillDefinition::visual),
            Requires.CODEC.optionalFieldOf("requires", Requires.EMPTY).forGetter(SkillDefinition::requires),
            Codec.INT.optionalFieldOf("slot", NO_SLOT).forGetter(SkillDefinition::slot),
            Codec.STRING.optionalFieldOf("special", "").forGetter(SkillDefinition::special)
    ).apply(instance, SkillDefinition::new));

    public int castTicks() {
        return cast.windup() + cast.active() + cast.recovery();
    }

    /** HUD 用：skill.csm.denji.chainsaw_rush */
    public String translationKey() {
        return "skill." + id.getNamespace() + "." + id.getPath().replace('/', '.');
    }

    public record Cost(int energy, int cooldown) {
        public static final Codec<Cost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("energy", 0).forGetter(Cost::energy),
                Codec.INT.optionalFieldOf("cooldown", 0).forGetter(Cost::cooldown)
        ).apply(instance, Cost::new));
    }

    public record Cast(int windup, int active, int recovery, double moveLock,
                       boolean invulnerable, boolean interruptible) {
        public static final Codec<Cast> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("windup", 0).forGetter(Cast::windup),
                Codec.INT.optionalFieldOf("active", 1).forGetter(Cast::active),
                Codec.INT.optionalFieldOf("recovery", 0).forGetter(Cast::recovery),
                Codec.DOUBLE.optionalFieldOf("move_lock", 1.0D).forGetter(Cast::moveLock),
                Codec.BOOL.optionalFieldOf("invulnerable", false).forGetter(Cast::invulnerable),
                Codec.BOOL.optionalFieldOf("interruptible", true).forGetter(Cast::interruptible)
        ).apply(instance, Cast::new));
    }

    public record Hit(String shape, double range, double angle, double width, double damage,
                      double knockback, String damageType, List<Effect> effects) {
        public static final Codec<Hit> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("shape", "single").forGetter(Hit::shape),
                Codec.DOUBLE.optionalFieldOf("range", 3.0D).forGetter(Hit::range),
                Codec.DOUBLE.optionalFieldOf("angle", 0.0D).forGetter(Hit::angle),
                Codec.DOUBLE.optionalFieldOf("width", 0.0D).forGetter(Hit::width),
                Codec.DOUBLE.optionalFieldOf("damage", 0.0D).forGetter(Hit::damage),
                Codec.DOUBLE.optionalFieldOf("knockback", 0.0D).forGetter(Hit::knockback),
                Codec.STRING.optionalFieldOf("damage_type", "csm:technique").forGetter(Hit::damageType),
                Effect.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(Hit::effects)
        ).apply(instance, Hit::new));

        /** 作用于自身的技能（buff/吸血）：sphere 且半径 <= 1 */
        public boolean selfOnly() {
            return "sphere".equals(shape) && range <= 1.0D;
        }
    }

    public record Effect(String id, int duration, int amplifier) {
        public static final Codec<Effect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(Effect::id),
                Codec.INT.optionalFieldOf("duration", 100).forGetter(Effect::duration),
                Codec.INT.optionalFieldOf("amplifier", 0).forGetter(Effect::amplifier)
        ).apply(instance, Effect::new));
    }

    public record Visual(String animation, String particle, String sound, double shake) {
        public static final Visual EMPTY = new Visual("", "", "", 0.0D);

        public static final Codec<Visual> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("animation_player", "").forGetter(Visual::animation),
                Codec.STRING.optionalFieldOf("particle", "").forGetter(Visual::particle),
                Codec.STRING.optionalFieldOf("sound", "").forGetter(Visual::sound),
                Codec.DOUBLE.optionalFieldOf("screen_shake", 0.0D).forGetter(Visual::shake)
        ).apply(instance, Visual::new));
    }

    public record Requires(int level, String flag, String item, String contract) {
        public static final Requires EMPTY = new Requires(0, "", "", "");

        public static final Codec<Requires> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("level", 0).forGetter(Requires::level),
                Codec.STRING.optionalFieldOf("flag", "").forGetter(Requires::flag),
                Codec.STRING.optionalFieldOf("item", "").forGetter(Requires::item),
                Codec.STRING.optionalFieldOf("contract", "").forGetter(Requires::contract)
        ).apply(instance, Requires::new));
    }
}
