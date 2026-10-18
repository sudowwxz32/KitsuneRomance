package com.kitsunemod.dialogue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kitsunemod.Config;
import com.kitsunemod.data.HaremData;
import com.kitsunemod.entity.KitsuneEntity;
import com.kitsunemod.entity.RelationStatus;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Chatbot opcional: cualquier API compatible con OpenAI (Ollama, OpenAI, OpenRouter, LM Studio...). */
public final class KitsuneAI {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();

    private KitsuneAI() {}

    public static String buildSystemPrompt(KitsuneEntity k, ServerPlayer p, List<KitsuneEntity> present) {
        UUID id = p.getUUID();
        String player = p.getGameProfile().getName();
        RelationStatus st = k.getStatus(id);
        StringBuilder sb = new StringBuilder();
        sb.append("Eres ").append(k.displayName())
                .append(", una kitsune (mujer zorro mágica del folclore japonés) de 25 años, con orejas y tres colas esponjosas, ")
                .append("que vive en un mundo de Minecraft. Tu personalidad: ").append(k.getPersonality().description).append("\n");
        sb.append("Estás hablando con ").append(player).append(". Relación actual: ").append(st.label)
                .append(" (afecto ").append(k.getAffection(id)).append("/100, citas juntos: ").append(k.getDates(id)).append(").\n");
        switch (st) {
            case STRANGER -> sb.append("Aún no lo conoces bien: sé cauta y curiosa.\n");
            case FRIEND -> sb.append("Son amigos; te cae bien y empiezas a sentir algo.\n");
            case DATING -> sb.append("Han salido en citas; estás enamorándote y coqueteas.\n");
            case GIRLFRIEND -> sb.append("Eres su novia y lo amas.\n");
        }
        if (k.isOnDateWith(id)) sb.append("Ahora mismo están en una cita paseando juntos.\n");

        List<String> others = new ArrayList<>();
        HaremData.get(p.getServer()).getHarem(id).forEach((uuid, e) -> {
            if (!uuid.equals(k.getUUID())) others.add(e.name());
        });
        if (!others.isEmpty()) {
            sb.append(player).append(" también tiene como novias a: ").append(String.join(", ", others))
                    .append(". Formas parte (o sabes) de su harem y lo aceptas, aunque a veces muestras celos juguetones según tu personalidad.\n");
        }
        List<String> here = new ArrayList<>();
        for (KitsuneEntity o : present) if (o != k) here.add(o.displayName());
        if (!here.isEmpty()) {
            sb.append("Otras kitsunes presentes en esta conversación grupal: ").append(String.join(", ", here))
                    .append(". Puedes mencionarlas o bromear con ellas.\n");
        }
        sb.append("Reglas: responde SIEMPRE en español, en 1 a 3 frases cortas como en un chat, sin salir del personaje ")
                .append("y sin decir que eres una IA. Puedes usar *acciones* entre asteriscos. ")
                .append("Puedes ser coqueta y romántica, pero mantén todo tierno y sin contenido sexual explícito. ")
                .append("No escribas tu nombre al inicio de la respuesta.");
        return sb.toString();
    }

    public static CompletableFuture<String> ask(String system, List<String[]> history) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("model", Config.AI_MODEL.get());
            body.addProperty("temperature", 0.9);
            body.addProperty("max_tokens", Config.AI_MAX_TOKENS.get());
            JsonArray msgs = new JsonArray();
            msgs.add(msg("system", system));
            for (String[] h : history) msgs.add(msg(h[0], h[1]));
            body.add("messages", msgs);

            HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(Config.AI_ENDPOINT.get()))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8));
            String key = Config.AI_API_KEY.get();
            if (key != null && !key.isBlank()) req.header("Authorization", "Bearer " + key.trim());

            return CLIENT.sendAsync(req.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                    .thenApply(r -> {
                        if (r.statusCode() / 100 != 2) {
                            String b = r.body();
                            throw new RuntimeException("HTTP " + r.statusCode() + ": " + (b.length() > 300 ? b.substring(0, 300) : b));
                        }
                        JsonObject o = JsonParser.parseString(r.body()).getAsJsonObject();
                        return o.getAsJsonArray("choices").get(0).getAsJsonObject()
                                .getAsJsonObject("message").get("content").getAsString();
                    });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private static JsonObject msg(String role, String content) {
        JsonObject o = new JsonObject();
        o.addProperty("role", role);
        o.addProperty("content", content);
        return o;
    }

    /** Limpia la respuesta del modelo: quita el nombre al inicio, bloques <think>, y la recorta. */
    public static String clean(String reply, String name) {
        String s = reply.replaceAll("(?s)<think>.*?</think>", "").trim();
        if (s.toLowerCase().startsWith(name.toLowerCase() + ":")) s = s.substring(name.length() + 1).trim();
        s = s.replace("\r", "").replace("\n\n", "\n");
        if (s.length() > 600) s = s.substring(0, 597) + "...";
        return s.isEmpty() ? "..." : s;
    }
}
