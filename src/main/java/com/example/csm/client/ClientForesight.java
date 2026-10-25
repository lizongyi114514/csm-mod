package com.example.csm.client;

/** 客户端预知状态：倒计时与预测数值。 */
public final class ClientForesight {

    private static int ticks;
    private static float damage;
    private static int threats;

    private ClientForesight() {
    }

    public static void set(int newTicks, float newDamage, int newThreats) {
        ticks = newTicks;
        damage = newDamage;
        threats = newThreats;
    }

    public static void tick() {
        if (ticks > 0) {
            ticks--;
        }
    }

    public static int ticks() {
        return ticks;
    }

    public static float damage() {
        return damage;
    }

    public static int threats() {
        return threats;
    }
}
