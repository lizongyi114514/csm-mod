package com.example.csm.client;

import com.example.csm.network.SkillStatePacket;

/** 客户端镜像。只负责显示，不做任何判定。 */
public final class ClientSkillState {

    private static int energy;
    private static int maxEnergy = 100;
    private static int form;
    private static String[] ids = {"", "", "", ""};
    private static int[] cooldowns = new int[4];
    private static int[] contracts = new int[3];

    private ClientSkillState() {
    }

    public static void accept(SkillStatePacket packet) {
        energy = packet.energy();
        maxEnergy = Math.max(1, packet.maxEnergy());
        form = packet.form();
        ids = packet.ids();
        cooldowns = packet.cooldowns();
        contracts = packet.contracts();
    }

    /** 每客户端 tick 递减，让冷却看起来是连续的 */
    public static void tick() {
        for (int i = 0; i < cooldowns.length; i++) {
            if (cooldowns[i] > 0) {
                cooldowns[i]--;
            }
        }
    }

    public static int energy() {
        return energy;
    }

    public static int maxEnergy() {
        return maxEnergy;
    }

    public static int form() {
        return form;
    }

    public static String id(int slot) {
        return slot >= 0 && slot < ids.length ? ids[slot] : "";
    }

    public static int cooldown(int slot) {
        return slot >= 0 && slot < cooldowns.length ? cooldowns[slot] : 0;
    }

    public static int contract(int index) {
        return index >= 0 && index < contracts.length ? contracts[index] : 0;
    }
}
