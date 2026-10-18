package com.kitsunemod.network;

import com.kitsunemod.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Abre (o actualiza) la ventana de chat con la lista de kitsunes presentes. */
public record OpenChatS2C(boolean open, int primaryId, List<KitsuneInfo> present) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(open);
        buf.writeVarInt(primaryId);
        buf.writeVarInt(present.size());
        for (KitsuneInfo i : present) i.encode(buf);
    }

    public static OpenChatS2C decode(FriendlyByteBuf buf) {
        boolean open = buf.readBoolean();
        int primary = buf.readVarInt();
        int n = Math.min(buf.readVarInt(), 64);
        List<KitsuneInfo> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) list.add(KitsuneInfo.decode(buf));
        return new OpenChatS2C(open, primary, list);
    }

    public static void handle(OpenChatS2C msg, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleSnapshot(msg));
    }
}
