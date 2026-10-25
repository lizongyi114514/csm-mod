package com.example.csm.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * 服务端 -> 客户端：让某个玩家播放一个 playeranimator 动作。
 * 只发动作名，客户端自己去 assets/csm/player_animation/<name>.json 找。
 */
public record PlayAnimPacket(int entityId, String animation) {

    public PlayAnimPacket(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readUtf());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeUtf(animation);
    }

    public static void send(ServerPlayer player, String animation) {
        if (animation == null || animation.isEmpty()) {
            return;
        }
        CsmNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new PlayAnimPacket(player.getId(), animation));
    }

    public static void handle(PlayAnimPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> com.example.csm.client.CsmPlayerAnimations.play(packet.entityId(), packet.animation()));
        context.setPacketHandled(true);
    }
}
