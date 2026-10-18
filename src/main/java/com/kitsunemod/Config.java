package com.kitsunemod;

import net.minecraftforge.common.ForgeConfigSpec;

/** Archivo: config/kitsunemod-common.toml */
public class Config {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue AI_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<String> AI_ENDPOINT;
    public static final ForgeConfigSpec.ConfigValue<String> AI_API_KEY;
    public static final ForgeConfigSpec.ConfigValue<String> AI_MODEL;
    public static final ForgeConfigSpec.IntValue AI_MAX_TOKENS;

    public static final ForgeConfigSpec.IntValue HAREM_MAX;
    public static final ForgeConfigSpec.IntValue DATE_AFFECTION;
    public static final ForgeConfigSpec.IntValue CONFESS_AFFECTION;
    public static final ForgeConfigSpec.IntValue CONFESS_DATES;
    public static final ForgeConfigSpec.IntValue DATE_MINUTES;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment("Chat con IA (opcional). Compatible con cualquier API estilo OpenAI /v1/chat/completions.",
                "Ejemplos de endpoint:",
                "  Ollama local (gratis):  http://localhost:11434/v1/chat/completions   (modelo: llama3.1, qwen2.5, etc.)",
                "  OpenAI:                 https://api.openai.com/v1/chat/completions    (modelo: gpt-4o-mini)",
                "  OpenRouter:             https://openrouter.ai/api/v1/chat/completions",
                "Si está desactivado o falla, las kitsunes usan su diálogo integrado.")
                .push("ai");
        AI_ENABLED = b.define("enabled", false);
        AI_ENDPOINT = b.define("endpoint", "http://localhost:11434/v1/chat/completions");
        AI_API_KEY = b.comment("Clave de API (déjala vacía para Ollama local).").define("apiKey", "");
        AI_MODEL = b.define("model", "llama3.1");
        AI_MAX_TOKENS = b.defineInRange("maxTokens", 200, 32, 2000);
        b.pop();

        b.push("romance");
        HAREM_MAX = b.comment("Máximo de novias kitsune en tu harem.").defineInRange("haremMax", 8, 1, 50);
        DATE_AFFECTION = b.comment("Afecto mínimo para aceptar una cita.").defineInRange("dateAffection", 30, 0, 100);
        CONFESS_AFFECTION = b.comment("Afecto mínimo para aceptar ser tu novia.").defineInRange("confessAffection", 75, 0, 100);
        CONFESS_DATES = b.comment("Citas exitosas necesarias antes de pedir noviazgo.").defineInRange("confessDates", 2, 0, 20);
        DATE_MINUTES = b.comment("Duración de una cita en minutos reales.").defineInRange("dateMinutes", 3, 1, 30);
        b.pop();

        SPEC = b.build();
    }
}
