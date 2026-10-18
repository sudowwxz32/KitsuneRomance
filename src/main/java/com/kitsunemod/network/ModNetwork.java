package com.kitsunemod.network;

import com.kitsunemod.KitsuneMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(KitsuneMod.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(ChatMessageC2S.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ChatMessageC2S::encode).decoder(ChatMessageC2S::decode)
                .consumerMainThread(ChatMessageC2S::handle).add();
        CHANNEL.messageBuilder(ActionC2S.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ActionC2S::encode).decoder(ActionC2S::decode)
                .consumerMainThread(ActionC2S::handle).add();
        CHANNEL.messageBuilder(OpenChatS2C.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenChatS2C::encode).decoder(OpenChatS2C::decode)
                .consumerMainThread(OpenChatS2C::handle).add();
        CHANNEL.messageBuilder(ChatLineS2C.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ChatLineS2C::encode).decoder(ChatLineS2C::decode)
                .consumerMainThread(ChatLineS2C::handle).add();
    }
}
