# Kitsune Romance — Minecraft Java 1.20.1 (Forge 47.x)

Mod de romance con cuatro chicas kitsune de 25 años, chat grupal (con IA opcional), regalos, citas, noviazgo y harem.

## Las kitsunes

| Nombre | Personalidad | Aspecto |
|---|---|---|
| **Yuki** | Tímida, dulce, se sonroja fácil | Cabello plateado, ojos azules, kimono blanco y azul |
| **Akane** | Orgullosa y tsundere, adora el tofu inari | Cabello rojo, traje de miko |
| **Hana** | Alegre, cariñosa, ama las flores | Cabello dorado, kimono rosa |
| **Kuro** | Misteriosa y coqueta, ama la noche | Cabello negro, ojos violetas |

Todas tienen orejas de zorro y tres colas animadas que se mueven más rápido cuando están felices (en una cita o cerca de su pareja).

**Dónde aparecen:** bosques, bosques de cerezos, taigas y praderas. También puedes usar el **Huevo de Kitsune** de la pestaña creativa *Kitsune Romance*, o `/summon kitsunemod:kitsune`.

## Cómo jugar

1. **Clic derecho** a una kitsune → se abre la ventana de chat. Arriba aparecen **todas las kitsunes presentes** (12 bloques); haz clic en una para hablarle.
2. Escribe y pulsa Enter. Usa `@Hana hola` para hablarle a una concreta o `@todas` (o el botón **Para: Todas**) para que respondan todas.
3. **Regalos:** clic derecho con el objeto en la mano.
   - Tofu Inari +12 (receta: 2 trigo + 1 azúcar → 2), Manzana dorada +15, Pastel +8, Diamante/Esmeralda +6, Flores +5, Galletas/Miel +4, Bayas +3
   - Carne podrida, ojo de araña, huesos… **restan** afecto. Pegarle también.
4. **Cita** (desde 30 de afecto): pulsa *Cita* y quédate cerca de ella durante 3 minutos. Te sigue a todas partes. Si estás con ella al menos el 60 % del tiempo: +10 afecto y la cita cuenta.
5. **Noviazgo:** con 75 de afecto, 2 citas exitosas y un **Anillo de Promesa** en el inventario (receta: diamante arriba, 3 lingotes de oro en forma de U), pulsa *Pedir noviazgo*.
6. **Harem:** puedes tener hasta 8 novias (configurable). Cada novia adicional pide 10 de afecto más. Las novias se ponen celosas si le das regalos a otra delante de ellas. Mira tu harem con `/kitsune harem`.
7. Las novias y acompañantes **te siguen** (botón *Sígueme / Quédate aquí*), se teletransportan si te alejas y **te defienden** de los monstruos.

`/kitsune ayuda` muestra esta guía en el juego.

## Chat con IA (opcional)

Sin configurar nada, las kitsunes responden con su diálogo integrado (reconocen saludos, cumplidos, "te quiero", preguntas sobre citas, comida, colas, harem, etc.).

Para conversaciones libres con IA, edita `config/kitsunemod-common.toml` (se crea al abrir el juego una vez):

```toml
[ai]
    enabled = true
    endpoint = "http://localhost:11434/v1/chat/completions"   # Ollama local, gratis
    apiKey = ""
    model = "llama3.1"
```

- **Ollama (gratis, en tu PC):** instala https://ollama.com y ejecuta `ollama pull llama3.1`.
- **OpenAI:** `endpoint = "https://api.openai.com/v1/chat/completions"`, `apiKey = "sk-..."`, `model = "gpt-4o-mini"`.
- **OpenRouter / LM Studio:** cualquier API compatible con OpenAI funciona.

La IA sabe quién es cada kitsune, su relación contigo, cuántas citas llevan, si es tu novia, quiénes más forman tu harem y qué otras kitsunes están presentes. Si la IA falla, vuelve al diálogo integrado.
En multijugador la IA se configura en el **servidor**.

## Compilar el .jar

Necesitas **JDK 17** (por ejemplo Temurin 17).

```bash
# Windows
gradlew.bat build
# Linux / Mac
./gradlew build
```

El mod queda en `build/libs/kitsunemod-1.20.1-1.0.0.jar`. Cópialo a la carpeta `mods` de tu Minecraft 1.20.1 con Forge 47.x.

Para probarlo directamente: `gradlew runClient`.

**Sin instalar nada:** sube esta carpeta a un repositorio de GitHub; la acción incluida (`.github/workflows/build.yml`) compila el mod y deja el .jar en la pestaña *Actions → Artifacts*.

## Estructura

```
src/main/java/com/kitsunemod/
  entity/     KitsuneEntity (afecto, citas, noviazgo, defensa), personalidades, regalos
  dialogue/   Diálogo integrado en español + cliente de IA
  network/    Paquetes cliente/servidor del chat
  client/     Modelo con orejas y colas, renderer, ventana de chat
  data/       HaremData (guardado del harem en el mundo)
  event/      Atributos, spawns, comandos /kitsune
src/main/resources/
  assets/     Texturas, modelos de objetos, idiomas (es_*, en_us)
  data/       Recetas y spawns por bioma
```
