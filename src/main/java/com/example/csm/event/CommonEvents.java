package com.example.csm.event;

import com.example.csm.Csm;
import com.example.csm.config.CsmConfig;
import com.example.csm.contract.Contract;
import com.example.csm.contract.ContractManager;
import com.example.csm.form.Form;
import com.example.csm.form.FormManager;
import com.example.csm.network.SkillStatePacket;
import com.example.csm.player.PlayerResources;
import com.example.csm.registry.CsmEntities;
import com.example.csm.registry.CsmItems;
import com.example.csm.entity.PochitaEntity;
import com.example.csm.skill.SkillDefinition;
import com.example.csm.skill.SkillEngine;
import com.example.csm.skill.SkillManager;
import com.example.csm.world.DevilNestFeature;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Csm.MODID)
public final class CommonEvents {

    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onBuildTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(CsmItems.CHAINSAW);
            event.accept(CsmItems.EYEPATCH);
        }
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SkillManager());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        FormManager.tick(player);
        SkillEngine.tick(player);
        if (player.tickCount % 20 == 0) {
            SkillStatePacket.send(player);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        PlayerResources.copy(event.getOriginal(), event.getEntity());
        if (event.getEntity() instanceof ServerPlayer player) {
            SkillEngine.clear(player);
            ContractManager.apply(player);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ContractManager.apply(player);
        if (PlayerResources.form(player).isEmpty()) {
            FormManager.setForm(player, Form.HUNTER);
        }
        SkillStatePacket.send(player);
    }

    private static int summonBoss(CommandSourceStack source, String bossName) {
        ServerPlayer player = source.getPlayer();
        if (player == null || !(player.level() instanceof ServerLevel level)) {
            return 0;
        }
        if (!CsmConfig.bossSummon()) {
            source.sendFailure(Component.literal("服务器已禁用 Boss 召唤（config/csm-common.toml）"));
            return 0;
        }
        Mob boss = "gun".equals(bossName)
                ? CsmEntities.GUN_DEVIL.get().create(level)
                : CsmEntities.KATANA_MAN.get().create(level);
        if (boss == null) {
            return 0;
        }
        Vec3 spawn = player.position().add(player.getLookAngle().scale(3.0D));
        boss.moveTo(spawn.x, player.getY(), spawn.z, player.getYRot(), 0.0F);
        level.addFreshEntity(boss);
        String label = "gun".equals(bossName) ? "枪之恶魔（换弹时才是输出窗口）" : "武士刀男（前摇 24 tick，记得侧闪）";
        source.sendSuccess(() -> Component.literal(label), false);
        return 1;
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("csm")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("form")
                        .then(Commands.argument("id", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    for (Form form : Form.values()) {
                                        builder.suggest(form.id());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    Form form = Form.byId(StringArgumentType.getString(context, "id"));
                                    FormManager.setForm(player, form);
                                    context.getSource().sendSuccess(() -> Component.literal("CSM form -> " + form.id()), false);
                                    return 1;
                                })))
                .then(Commands.literal("cast")
                        .then(Commands.argument("skill", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (Map.Entry<String, List<SkillDefinition>> entry : SkillManager.all().entrySet()) {
                                        for (SkillDefinition definition : entry.getValue()) {
                                            builder.suggest(definition.id().toString());
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ResourceLocation id = ResourceLocation.tryParse(StringArgumentType.getString(context, "skill"));
                                    boolean ok = id != null && SkillEngine.cast(player, id);
                                    context.getSource().sendSuccess(() -> Component.literal(ok ? "施放成功" : "施放失败（形态/能量/冷却/前置）"), false);
                                    return ok ? 1 : 0;
                                })))
                .then(Commands.literal("summon")
                        .executes(context -> summonBoss(context.getSource(), "katana"))
                        .then(Commands.argument("boss", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    builder.suggest("katana");
                                    builder.suggest("gun");
                                    return builder.buildFuture();
                                })
                                .executes(context -> summonBoss(context.getSource(),
                                        StringArgumentType.getString(context, "boss")))))
                .then(Commands.literal("pochita").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    PochitaEntity dog = PochitaEntity.summon(player);
                    context.getSource().sendSuccess(() -> Component.literal(
                            dog == null ? "召唤失败" : "波奇塔来了"), false);
                    return dog == null ? 0 : 1;
                }))
                .then(Commands.literal("nest").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    if (!(player.level() instanceof ServerLevel level)) {
                        return 0;
                    }
                    net.minecraft.core.BlockPos origin = player.blockPosition().offset(0, 0, 6);
                    DevilNestFeature.build(level, origin, player.getRandom());
                    context.getSource().sendSuccess(() -> Component.literal("恶魔巢穴已生成（6 格前）"), false);
                    return 1;
                }))
                .then(Commands.literal("contract")
                        .then(Commands.literal("reset").executes(context -> {
                            ContractManager.reset(context.getSource().getPlayerOrException());
                            return 1;
                        }))
                        .then(Commands.literal("info").executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String text = "狐狸剩余 " + ContractManager.foxRemaining(player)
                                    + " / 诅咒层数 " + ContractManager.uses(player, Contract.CURSE)
                                    + " / 未来次数 " + ContractManager.uses(player, Contract.FUTURE)
                                    + (ContractManager.foxRevoked(player) ? "（狐狸已作废）" : "");
                            context.getSource().sendSuccess(() -> Component.literal(text), false);
                            return 1;
                        })))
                .then(Commands.literal("skills").executes(context -> {
                    StringBuilder builder = new StringBuilder();
                    for (Map.Entry<String, List<SkillDefinition>> entry : SkillManager.all().entrySet()) {
                        builder.append('[').append(entry.getKey()).append("] ");
                        for (SkillDefinition definition : entry.getValue()) {
                            builder.append(definition.id().getPath()).append(' ');
                        }
                        builder.append('\n');
                    }
                    String text = builder.length() == 0 ? "尚未载入任何技能（检查 data/csm/skills）" : builder.toString();
                    context.getSource().sendSuccess(() -> Component.literal(text), false);
                    return 1;
                })));
    }
}
