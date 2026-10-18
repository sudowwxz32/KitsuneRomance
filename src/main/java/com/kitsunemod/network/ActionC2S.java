package com.kitsunemod.network;

import com.kitsunemod.entity.KitsuneEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Botones del menú: cita, pedir noviazgo, seguir/quedarse, terminar. */
public record ActionC2S(int entityId, int action) {
    public static final int DATE = 0, CONFESS = 1, FOLLOW = 2, BREAK_UP = 3, REFRESH = 4;

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(action);
    }

    public static ActionC2S decode(FriendlyByteBuf buf) {
        return new ActionC2S(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(ActionC2S msg, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sp = ctx.get().getSender();
        if (sp == null) return;
        Entity e = sp.level().getEntity(msg.entityId);
        if (!(e instanceof KitsuneEntity k) || !k.isAlive() || k.distanceToSqr(sp) > 20 * 20) return;
        switch (msg.action) {
            case DATE -> k.startDate(sp);
            case CONFESS -> k.confess(sp);
            case FOLLOW -> k.toggleFollow(sp);
            case BREAK_UP -> k.breakUp(sp);
            default -> {}
        }
        k.sendSnapshot(sp, false);
    }
}
