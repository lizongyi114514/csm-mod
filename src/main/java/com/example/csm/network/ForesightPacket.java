package com.example.csm.network;

import com.example.csm.client.ClientForesight;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/** S -> C：预知窗口（剩余 tick、预计伤害、威胁数）。 */
public final class ForesightPacket {

    private final int ticks;
    private final float damage;
    private final int threats;

    public ForesightPacket(int ticks, float damage, int threats) {
        this.ticks = ticks;
        this.damage = damage;
        this.threats = threats;
    }

    public ForesightPacket(FriendlyByteBuf buf) {
        this.ticks = buf.readVarInt();
        this.damage = buf.readFloat();
        this.threats = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.ticks);
        buf.writeFloat(this.damage);
        buf.writeVarInt(this.threats);
    }

    public static void send(ServerPlayer player, int ticks, float damage, int threats) {
        CsmNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new ForesightPacket(ticks, damage, threats));
    }

    public static void handle(ForesightPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientForesight.set(msg.ticks, msg.damage, msg.threats));
        ctx.get().setPacketHandled(true);
    }
}
