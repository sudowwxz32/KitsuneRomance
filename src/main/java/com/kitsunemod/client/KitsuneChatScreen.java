package com.kitsunemod.client;

import com.kitsunemod.entity.Personality;
import com.kitsunemod.entity.RelationStatus;
import com.kitsunemod.network.ActionC2S;
import com.kitsunemod.network.ChatMessageC2S;
import com.kitsunemod.network.KitsuneInfo;
import com.kitsunemod.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

/** Ventana de chat con las kitsunes presentes: conversación, citas, noviazgo y harem. */
public class KitsuneChatScreen extends Screen {
    private EditBox input;
    private int px, py, pw, ph;
    private int scroll;
    private String pendingText = "";

    public KitsuneChatScreen() {
        super(Component.literal("Kitsune Chat"));
    }

    @Override
    protected void init() {
        pw = Math.min(400, width - 20);
        ph = height - 30;
        px = (width - pw) / 2;
        py = 15;

        // Pestañas con las kitsunes presentes
        List<KitsuneInfo> present = ClientChatState.present;
        int tabW = Math.min(70, (pw - 12) / Math.max(1, Math.min(present.size(), 6)));
        for (int i = 0; i < present.size() && i < 6; i++) {
            KitsuneInfo k = present.get(i);
            boolean sel = k.entityId() == ClientChatState.targetId && !ClientChatState.toAll;
            String label = (sel ? "▶ " : "") + k.name();
            addRenderableWidget(Button.builder(Component.literal(label), b -> {
                ClientChatState.targetId = k.entityId();
                ClientChatState.toAll = false;
                refresh();
            }).bounds(px + 6 + i * tabW, py + 6, tabW - 2, 16).build());
        }

        KitsuneInfo t = ClientChatState.target();
        int by = py + ph - 46;
        int bw = (pw - 12) / 4;
        boolean gf = t != null && t.status() == RelationStatus.GIRLFRIEND.ordinal();

        Button date = Button.builder(Component.literal(t != null && t.onDate() ? "♥ En cita..." : "♥ Cita"),
                b -> action(ActionC2S.DATE)).bounds(px + 6, by, bw - 2, 18).build();
        date.active = t != null && !t.onDate();
        addRenderableWidget(date);

        Button confess = Button.builder(Component.literal(gf ? "Es tu novia" : "Pedir noviazgo"),
                b -> action(ActionC2S.CONFESS)).bounds(px + 6 + bw, by, bw - 2, 18).build();
        confess.active = t != null && !gf;
        addRenderableWidget(confess);

        addRenderableWidget(Button.builder(Component.literal(t != null && t.following() ? "Quédate aquí" : "Sígueme"),
                b -> action(ActionC2S.FOLLOW)).bounds(px + 6 + bw * 2, by, bw - 2, 18).build());

        Button breakUp = Button.builder(Component.literal("Terminar"),
                b -> action(ActionC2S.BREAK_UP)).bounds(px + 6 + bw * 3, by, bw - 2, 18).build();
        breakUp.active = gf;
        addRenderableWidget(breakUp);

        // Entrada de texto
        int iy = py + ph - 24;
        input = new EditBox(font, px + 6, iy, pw - 122, 18, Component.literal("Mensaje"));
        input.setMaxLength(ChatMessageC2S.MAX);
        input.setHint(Component.literal("Escribe... (@Nombre o @todas)"));
        input.setValue(pendingText);
        addRenderableWidget(input);
        setInitialFocus(input);

        String to = ClientChatState.toAll ? "Todas" : (t == null ? "-" : t.name());
        addRenderableWidget(Button.builder(Component.literal("Para: " + to), b -> {
            ClientChatState.toAll = !ClientChatState.toAll;
            refresh();
        }).bounds(px + pw - 114, iy, 60, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Enviar"), b -> send())
                .bounds(px + pw - 52, iy, 46, 18).build());
    }

    public void refresh() {
        if (input != null) pendingText = input.getValue();
        rebuildWidgets();
    }

    public void onNewLine() {
        scroll = 0;
    }

    private void action(int id) {
        KitsuneInfo t = ClientChatState.target();
        if (t != null) ModNetwork.CHANNEL.sendToServer(new ActionC2S(t.entityId(), id));
    }

    private void send() {
        String text = input.getValue().trim();
        KitsuneInfo t = ClientChatState.target();
        if (text.isEmpty() || t == null) return;
        ModNetwork.CHANNEL.sendToServer(new ChatMessageC2S(t.entityId(), text, ClientChatState.toAll));
        input.setValue("");
        pendingText = "";
        scroll = 0;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if ((key == 257 || key == 335) && input.isFocused()) { // Enter
            send();
            return true;
        }
        if (input.isFocused() && key != 256) { // todo menos Escape va a la caja de texto
            return input.keyPressed(key, scan, mods) || input.canConsumeInput();
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        scroll = Math.max(0, scroll + (delta > 0 ? 3 : -3));
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        g.fill(px, py, px + pw, py + ph, 0xE0141020);
        g.fill(px, py, px + pw, py + 1, 0xFFFF8FC8);
        g.fill(px, py + ph - 1, px + pw, py + ph, 0xFFFF8FC8);

        // Información de la kitsune seleccionada
        KitsuneInfo t = ClientChatState.target();
        if (t != null) {
            Personality per = Personality.byId(t.variant());
            RelationStatus st = RelationStatus.values()[Math.min(t.status(), RelationStatus.values().length - 1)];
            int x = px + 8, y = py + 27;
            x = g.drawString(font, t.name(), x, y, per.color) + 6;
            x = g.drawString(font, st.label, x, y, st.color) + 6;
            int hearts = t.affection() / 10;
            for (int i = 0; i < 10; i++) {
                g.drawString(font, "♥", x + i * 7, y, i < hearts ? 0xFFFF5C8A : 0xFF4A3A44);
            }
            x += 74;
            g.drawString(font, t.affection() + "/100  Citas: " + t.dates() + (t.onDate() ? "  [EN CITA]" : ""), x, y, 0xBBBBBB);
        }

        // Historial
        int top = py + 40, bottom = py + ph - 50, lw = pw - 16;
        List<FormattedCharSequence> wrapped = new ArrayList<>();
        for (ClientChatState.Line l : ClientChatState.LINES) {
            Component c = Component.literal(l.speaker() + ": ").withStyle(s -> s.withColor(l.color()).withBold(true))
                    .append(Component.literal(l.text()).withStyle(s -> s.withColor(0xF0F0F0).withBold(false)));
            wrapped.addAll(font.split(c, lw));
        }
        int lineH = 10;
        int visible = (bottom - top) / lineH;
        scroll = Math.min(scroll, Math.max(0, wrapped.size() - visible));
        int end = wrapped.size() - scroll;
        int start = Math.max(0, end - visible);
        g.enableScissor(px + 4, top, px + pw - 4, bottom);
        int y = bottom - (end - start) * lineH;
        for (int i = start; i < end; i++) {
            g.drawString(font, wrapped.get(i), px + 8, y, 0xFFFFFF);
            y += lineH;
        }
        g.disableScissor();
        if (ClientChatState.LINES.isEmpty()) {
            g.drawCenteredString(font, "Saluda a las kitsunes...", px + pw / 2, (top + bottom) / 2, 0x888888);
        }

        super.render(g, mx, my, pt);
    }

    @Override
    public void removed() {
        // mantiene el historial para la próxima vez
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
