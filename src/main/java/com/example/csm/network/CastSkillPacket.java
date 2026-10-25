package com.example.csm.network;

import com.example.csm.Csm;
import com.example.csm.skill.SkillEngine;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** 客户端按下技能键时发送。slot 是 0..3，服务端按当前形态解析成具体招式。 */
public record CastSkillPacket(int slot) {

    public CastSkillPacket(FriendlyByteBuf buffer) {
        this(buffer.readVarInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(slot);
    }

    public static void handle(CastSkillPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            if (!SkillEngine.cast(player, packet.slot())) {
                Csm.LOGGER.debug("[CSM] 施法失败 slot={}", packet.slot());
            }
        });
        context.setPacketHandled(true);
    }
}
