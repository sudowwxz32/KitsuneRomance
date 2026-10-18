package com.kitsunemod.network;

import net.minecraft.network.FriendlyByteBuf;

/** Resumen de una kitsune presente, enviado al cliente para la interfaz de chat. */
public record KitsuneInfo(int entityId, String name, int variant, int affection, int status,
                          int dates, boolean onDate, boolean following) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeUtf(name, 64);
        buf.writeVarInt(variant);
        buf.writeVarInt(affection);
        buf.writeVarInt(status);
        buf.writeVarInt(dates);
        buf.writeBoolean(onDate);
        buf.writeBoolean(following);
    }

    public static KitsuneInfo decode(FriendlyByteBuf buf) {
        return new KitsuneInfo(buf.readVarInt(), buf.readUtf(64), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }
}
