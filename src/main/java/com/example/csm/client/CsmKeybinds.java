package com.example.csm.client;

import com.example.csm.Csm;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** 1..4 = 技能槽，R = 变身。键位名在 lang 里。 */
@Mod.EventBusSubscriber(modid = Csm.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CsmKeybinds {

    public static final KeyMapping[] SKILLS = new KeyMapping[4];
    public static final KeyMapping FORM = new KeyMapping("key.csm.form", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.csm");
    public static final KeyMapping CODEX = new KeyMapping("key.csm.codex", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.csm");

    static {
        int[] keys = {GLFW.GLFW_KEY_1, GLFW.GLFW_KEY_2, GLFW.GLFW_KEY_3, GLFW.GLFW_KEY_4};
        for (int i = 0; i < SKILLS.length; i++) {
            SKILLS[i] = new KeyMapping("key.csm.skill" + (i + 1), InputConstants.Type.KEYSYM, keys[i], "key.csm");
        }
    }

    private CsmKeybinds() {
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        for (KeyMapping mapping : SKILLS) {
            event.register(mapping);
        }
        event.register(FORM);
        event.register(CODEX);
    }
}
