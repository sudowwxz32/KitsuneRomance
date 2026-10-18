package com.kitsunemod.client;

import com.kitsunemod.network.KitsuneInfo;

import java.util.ArrayList;
import java.util.List;

/** Estado del chat en el cliente (historial y kitsunes presentes). */
public final class ClientChatState {
    private ClientChatState() {}

    public record Line(String speaker, String text, int color) {}

    public static final List<Line> LINES = new ArrayList<>();
    public static List<KitsuneInfo> present = new ArrayList<>();
    public static int targetId = -1;
    public static boolean toAll = false;

    public static void addLine(Line line) {
        LINES.add(line);
        while (LINES.size() > 150) LINES.remove(0);
    }

    public static KitsuneInfo target() {
        for (KitsuneInfo i : present) if (i.entityId() == targetId) return i;
        return present.isEmpty() ? null : present.get(0);
    }
}
