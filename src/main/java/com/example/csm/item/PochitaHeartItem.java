package com.example.csm.item;

import com.example.csm.form.Form;
import com.example.csm.form.FormManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

/** 波奇塔的心脏：吃掉后获得电锯形态（原作里电次就是靠它）。 */
public class PochitaHeartItem extends Item {

    public PochitaHeartItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }
        if (FormManager.getForm(serverPlayer) == Form.DENJI) {
            serverPlayer.displayClientMessage(Component.translatable("csm.heart.already"), true);
            return InteractionResultHolder.fail(stack);
        }
        FormManager.setForm(serverPlayer, Form.DENJI);
        serverPlayer.displayClientMessage(Component.translatable("csm.heart.used"), false);
        level.playSound(null, serverPlayer.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.6F, 1.4F);
        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
