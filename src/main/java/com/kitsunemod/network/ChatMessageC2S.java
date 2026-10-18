package com.kitsunemod.network;

import com.kitsunemod.entity.KitsuneEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** El jugador escribe un mensaje en el chat de kitsunes. */
public record ChatMessageC2S(int entityId, String text, boolean toAll) {
    public static final int MAX = 256;

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeUtf(text, MAX);
        buf.writeBoolean(toAll);
    }

    public static ChatMessageC2S decode(FriendlyByteBuf buf) {
        return new ChatMessageC2S(buf.readVarInt(), buf.readUtf(MAX), buf.readBoolean());
    }

    public static void handle(ChatMessageC2S msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sp = ctx.get().getSender();
        if (sp == null) return;
        Entity e = sp.level().getEntity(msg.entityId);
        if (!(e instanceof KitsuneEntity k) || !k.isAlive() || k.distanceToSqr(sp) > 20 * 20) return;
        String text = msg.text.trim();
        if (text.isEmpty()) return;
        KitsuneEntity.handlePlayerChat(sp, k, text, msg.toAll);
    }
}
