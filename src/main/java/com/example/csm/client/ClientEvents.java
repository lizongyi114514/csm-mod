package com.example.csm.client;

import com.example.csm.Csm;
import com.example.csm.network.CastSkillPacket;
import com.example.csm.network.CsmNetwork;
import com.example.csm.network.FormTogglePacket;
import com.example.csm.client.codex.CodexScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 客户端每 tick：跑冷却、读按键、发包。 */
@Mod.EventBusSubscriber(modid = Csm.MODID, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        if (event.getPlayer() != null) {
            CsmPlayerAnimations.forget(event.getPlayer().getUUID());
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientSkillState.tick();

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        if (minecraft.screen == null && CsmKeybinds.CODEX.consumeClick()) {
            minecraft.setScreen(new CodexScreen());
            return;
        }
        if (minecraft.screen != null) {
            return;
        }

        for (int i = 0; i < CsmKeybinds.SKILLS.length; i++) {
            while (CsmKeybinds.SKILLS[i].consumeClick()) {
                CsmNetwork.CHANNEL.sendToServer(new CastSkillPacket(i));
            }
        }
        while (CsmKeybinds.FORM.consumeClick()) {
            CsmNetwork.CHANNEL.sendToServer(new FormTogglePacket());
        }
    }
}
