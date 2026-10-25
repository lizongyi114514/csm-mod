package com.example.csm.network;

import com.example.csm.form.FormManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** R 键：变身 / 解除。规则在 FormManager 里。 */
public record FormTogglePacket() {

    public FormTogglePacket(FriendlyByteBuf buffer) {
        this();
    }

    public void encode(FriendlyByteBuf buffer) {
        // 没有负载
    }

    public static void handle(FormTogglePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                FormManager.toggle(player);
            }
        });
        context.setPacketHandled(true);
    }
}
