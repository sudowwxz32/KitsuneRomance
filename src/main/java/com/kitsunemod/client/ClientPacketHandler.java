package com.kitsunemod.client;

import com.kitsunemod.network.ChatLineS2C;
import com.kitsunemod.network.OpenChatS2C;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ClientPacketHandler {
    private ClientPacketHandler() {}

    public static void handleSnapshot(OpenChatS2C msg) {
        Minecraft mc = Minecraft.getInstance();
        ClientChatState.present = msg.present();
        if (msg.open()) {
            ClientChatState.targetId = msg.primaryId();
            if (mc.screen instanceof KitsuneChatScreen s) s.refresh();
            else mc.setScreen(new KitsuneChatScreen());
        } else if (mc.screen instanceof KitsuneChatScreen s) {
            s.refresh();
        }
    }

    public static void handleLine(ChatLineS2C msg) {
        Minecraft mc = Minecraft.getInstance();
        ClientChatState.addLine(new ClientChatState.Line(msg.speaker(), msg.text(), msg.color()));
        if (mc.screen instanceof KitsuneChatScreen s) {
            s.onNewLine();
        } else if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(msg.speaker() + ": ")
                    .withStyle(st -> st.withColor(msg.color()))
                    .append(Component.literal(msg.text()).withStyle(st -> st.withColor(0xFFFFFF))), false);
        }
    }
}
