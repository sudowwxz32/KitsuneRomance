package com.kitsunemod.network;

import com.kitsunemod.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Una línea de diálogo de una kitsune (o el eco del jugador). */
public record ChatLineS2C(String speaker, String text, int color) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(speaker, 64);
        buf.writeUtf(text, 2048);
        buf.writeInt(color);
    }

    public static ChatLineS2C decode(FriendlyByteBuf buf) {
        return new ChatLineS2C(buf.readUtf(64), buf.readUtf(2048), buf.readInt());
    }

    public static void handle(ChatLineS2C msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleLine(msg));
    }
}
