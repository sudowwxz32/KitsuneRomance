package com.kitsunemod.event;

import com.kitsunemod.Config;
import com.kitsunemod.KitsuneMod;
import com.kitsunemod.data.HaremData;
import com.kitsunemod.entity.Personality;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = KitsuneMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("kitsune")
                .then(Commands.literal("harem").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    Map<UUID, HaremData.Entry> harem = HaremData.get(p.getServer()).getHarem(p.getUUID());
                    if (harem.isEmpty()) {
                        p.sendSystemMessage(Component.literal("Tu harem está vacío. ¡Conquista el corazón de alguna kitsune!")
                                .withStyle(ChatFormatting.GRAY));
                        return 0;
                    }
                    p.sendSystemMessage(Component.literal("♥ Tu harem (" + harem.size() + "/" + Config.HAREM_MAX.get() + ") ♥")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
                    harem.values().forEach(e -> {
                        Personality per = Personality.byId(e.variant());
                        int hearts = e.affection() / 10;
                        p.sendSystemMessage(Component.literal(" - " + e.name())
                                .withStyle(s -> s.withColor(per.color))
                                .append(Component.literal(" (" + per.displayName + ") ").withStyle(ChatFormatting.GRAY))
                                .append(Component.literal("♥".repeat(hearts)).withStyle(ChatFormatting.RED))
                                .append(Component.literal("♥".repeat(10 - hearts)).withStyle(ChatFormatting.DARK_GRAY))
                                .append(Component.literal(" " + e.affection() + "/100").withStyle(ChatFormatting.GRAY)));
                    });
                    return harem.size();
                }))
                .then(Commands.literal("ayuda").executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(String.join("\n",
                            "♥ Kitsune Romance - Guía rápida ♥",
                            "• Clic derecho a una kitsune: abre el chat (todas las cercanas aparecen arriba).",
                            "• Escribe @Nombre para hablarle a una, o @todas para el grupo.",
                            "• Clic derecho con un regalo: tofu inari, flores, pastel, manzana dorada...",
                            "• 30 de afecto: invítala a una CITA y quédate cerca hasta que termine.",
                            "• 75 de afecto + 2 citas + Anillo de Promesa: pídele que sea tu NOVIA.",
                            "• Puedes tener varias novias: tu HAREM (/kitsune harem).",
                            "• Las novias te siguen y te defienden de los monstruos.")).withStyle(ChatFormatting.LIGHT_PURPLE), false);
                    return 1;
                })));
    }
}
