package com.example.csm.network;

import com.example.csm.Csm;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 只有三个包：
 *   CastSkillPacket   C -> S  按键（0..3），槽位在服务端解析
 *   FormTogglePacket  C -> S  变身/解除
 *   SkillStatePacket  S -> C  能量/形态/4 个槽位的 id 与剩余冷却
 */
public final class CsmNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Csm.MODID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    private CsmNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(CastSkillPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CastSkillPacket::encode)
                .decoder(CastSkillPacket::new)
                .consumer(CastSkillPacket::handle)
                .add();

        CHANNEL.messageBuilder(FormTogglePacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(FormTogglePacket::encode)
                .decoder(FormTogglePacket::new)
                .consumer(FormTogglePacket::handle)
                .add();

        CHANNEL.messageBuilder(SkillStatePacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SkillStatePacket::encode)
                .decoder(SkillStatePacket::new)
                .consumer(SkillStatePacket::handle)
                .add();

        CHANNEL.messageBuilder(PlayAnimPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PlayAnimPacket::encode)
                .decoder(PlayAnimPacket::new)
                .consumer(PlayAnimPacket::handle)
                .add();

        Csm.LOGGER.debug("[CSM] 注册 {} 个网络包", id);
    }
}
