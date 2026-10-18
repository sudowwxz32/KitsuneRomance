package com.kitsunemod.dialogue;

import com.kitsunemod.entity.KitsuneEntity;
import com.kitsunemod.entity.RelationStatus;
import net.minecraft.server.level.ServerPlayer;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Diálogo integrado (funciona sin internet). Cada clave tiene líneas para
 * Yuki, Akane, Hana y Kuro, en ese orden. {p} = nombre del jugador.
 */
public final class KitsuneDialogue {
    private KitsuneDialogue() {}

    private static final Map<String, String[][]> LINES = new HashMap<>();

    private static void L(String key, String[] yuki, String[] akane, String[] hana, String[] kuro) {
        LINES.put(key, new String[][]{yuki, akane, hana, kuro});
    }

    private static String[] a(String... s) { return s; }

    static {
        L("greet_0",
                a("¿Eh? A-ah, hola... no esperaba visitas. Soy Yuki.", "*esconde la mitad de la cara tras su cola* H-hola, viajero..."),
                a("¿Y tú quién eres? Hmph, no es que me importe... Soy Akane.", "¡Eh, humano! Mantén la distancia de mis colas, ¿entendido?"),
                a("¡Holaaa! ¡Un visitante! Soy Hana, ¿quieres ser mi amigo? ~♪", "*agita las colas emocionada* ¡Hola, hola! ¡Qué bien que viniste!"),
                a("Fufu... un humano curioso. Puedes llamarme Kuro.", "*te observa con ojos violetas* Qué valiente, acercarte así a una kitsune..."));
        L("greet_1",
                a("Ah, {p}... me alegra verte de nuevo.", "*sus orejas se levantan* ¡{p}! H-hola..."),
                a("Ah, eres tú, {p}. N-no estaba esperándote ni nada.", "Llegas tarde, {p}. ...¿Qué? Solo lo digo."),
                a("¡{p}! ¡Te extrañé un montón! ~♥", "*corre hacia ti* ¡Ya llegaste, {p}!"),
                a("Buenas, {p}. Justo pensaba en ti... casualidad, claro.", "Fufu, {p}. Siempre apareces cuando sale la luna."));
        L("greet_2",
                a("{p}... *se sonroja* ¿Vienes a verme a mí?", "Pensaba en nuestra última cita... hola, {p}."),
                a("¡{p}! ...Digo, hola. Tranquilo, no estoy sonriendo. Nada de eso.", "Hmph, {p}. Tienes suerte de que tenga tiempo para ti hoy."),
                a("¡{p}! *te abraza* ¡Ya quería verte!", "¿Hoy también saldremos juntos, {p}? ~♥"),
                a("Mi humano favorito... hola, {p}.", "*te rodea con una cola* Te estaba esperando, {p}."));
        L("greet_3",
                a("*te abraza suavemente* Bienvenido, {p}... mi corazón se calienta cuando estás cerca.", "{p}... mi novio. Todavía me sonrojo al decirlo."),
                a("¡Por fin, {p}! Una novia no debería esperar tanto, ¿sabes? ...Te extrañé, tonto.", "*cruza los brazos, pero la cola no deja de moverse* Hola, {p}."),
                a("¡Mi {p}! *salta a tus brazos* ¡Te quiero muchísimo! ~♥", "¡{p}! ¿Me trajiste algo? ¡Es broma! Tú eres mi regalo ~♪"),
                a("Mi amor... *te besa la mejilla* llegaste justo a tiempo.", "Fufu, {p}. Eres mío, ¿lo sabías? Bueno... compartido, quizás."));

        L("love_low",
                a("¿Q-qué? *se cubre la cara con la cola* Apenas nos conocemos...", "E-eso es muy repentino, {p}..."),
                a("¡¿Q-qué dices?! ¡Idiota! *orejas rojas*", "Hmph. Tendrás que esforzarte mucho más que eso."),
                a("¡Awww! Eres un encanto, pero primero seamos amigos, ¿vale? ~♪", "¡Jiji! ¡Qué atrevido!"),
                a("Fufu... qué palabras tan dulces para alguien que acabas de conocer.", "Cuidado, humano. Las kitsunes roban corazones."));
        L("love_mid",
                a("Y-yo... también siento algo por ti, {p}. *sus colas tiemblan*", "Mi corazón late muy rápido cuando dices eso..."),
                a("...Tonto. No digas esas cosas tan fácil. ...Pero no pares.", "¡N-no me sonrojé! Es el calor. Sí, eso."),
                a("¡Kyaa! ¡Yo también te quiero, {p}! ~♥", "*abraza tu brazo* Dilo otra vez, ¿sí?"),
                a("Fufu... me gusta cómo suena eso en tu boca.", "Quizás... yo también me esté enamorando, {p}."));
        L("love_gf",
                a("Yo te amo más, {p}. Gracias por elegirme.", "*apoya la cabeza en tu hombro* Te amo... muchísimo."),
                a("...Yo también te amo. ¡Y no lo repetiré! ...Bueno, quizás una vez más.", "¡Obvio que me amas! Soy increíble. ...Y yo a ti, tonto."),
                a("¡Te amo, te amo, te amo! ~♥♥♥", "¡Eres lo mejor que me ha pasado, {p}!"),
                a("Y yo a ti, mi amor. Por mil años si hace falta.", "*entrelaza su cola con tu mano* Te amo, {p}."));
        L("compliment",
                a("¿B-bonita? *se le erizan las orejas* Gracias...", "N-nadie me había dicho eso antes..."),
                a("¡Obviamente! Soy la kitsune más linda del bosque. ...Gracias.", "Hmph, ya lo sabía. ...Pero dilo otra vez."),
                a("¡Jiji! ¡Tú también eres muy lindo, {p}! ~♪", "*da una vueltita* ¿Verdad que sí?"),
                a("Fufu, lo sé. Pero me gusta oírlo de ti.", "Halagador... sigue así y te ganarás una recompensa."));
        L("harem",
                a("Si todas te hacemos feliz... yo también estaré feliz. Pero no me olvides, ¿sí?", "Compartirte es difícil... pero las demás son buenas chicas."),
                a("¡¿Harem?! ...Está bien, PERO yo soy la número uno. ¿Entendido?", "Hmph. Mientras me prestes más atención que a las otras..."),
                a("¡Mientras más, más divertido! ¡Seremos una gran familia de colas! ~♪", "¡Me encanta que todas estemos juntas contigo!"),
                a("Fufu... un humano con varias kitsunes. Qué ambicioso. Me gusta.", "Las kitsunes vivimos en clanes. Un harem no es tan extraño para nosotras."));
        L("date_talk",
                a("¿Una cita? M-me gustaría mucho... pulsa \"Cita\" cuando quieras.", "Caminar contigo bajo los árboles sería precioso."),
                a("¿Me estás invitando a salir? ...Bueno, si insistes. Pulsa \"Cita\".", "Una cita conmigo es un privilegio, que lo sepas."),
                a("¡Sí, sí, una cita! ¡Vamos a ver flores! Pulsa \"Cita\" ~♥", "¡Me encantan las citas! ¿A dónde vamos?"),
                a("Una cita a la luz de la luna... pulsa \"Cita\" y te sigo.", "Sorpréndeme, humano."));
        L("name",
                a("Me llamo Yuki. Significa \"nieve\".", "Yuki... una kitsune de las montañas nevadas."),
                a("¡Akane! \"Rojo intenso\", como mi fuego. Recuérdalo.", "Soy Akane, y más te vale no olvidarlo."),
                a("¡Soy Hana! Significa \"flor\" ~♪", "Hana, como las flores que tanto me gustan."),
                a("Kuro. \"Negro\", como la noche que me protege.", "Puedes llamarme Kuro... o como quieras, si me lo pides bien."));
        L("age",
                a("Tengo 25 años... las kitsunes contamos los años de forma distinta, pero esa es mi edad.", "Veinticinco... ¿parezco mayor?"),
                a("¡Veinticinco! Una adulta hecha y derecha. ¡No me trates como niña!", "25 años, y ya tengo tres colas. Impresionante, ¿no?"),
                a("¡25 añitos! Bueno... con la energía de una niña ~♪", "Veinticinco, ¡y cada año más linda!"),
                a("Una dama nunca revela su edad. ...25. Fufu.", "Veinticinco. Joven para una kitsune, ¿sabías?"));
        L("food",
                a("El tofu inari es mi favorito... si tienes, me haría muy feliz.", "Un poco de tofu frito me calentaría el corazón."),
                a("¡¿Tienes tofu inari?! ¡Dámelo! ...Por favor.", "Hmph, tengo hambre. Un inari arreglaría mi humor."),
                a("¡Me encantan los dulces! Pasteles, galletas, bayas... ¡y el tofu inari!", "¿Hacemos un picnic? ~♪"),
                a("El tofu inari... la debilidad de toda kitsune. No se lo digas a nadie.", "Tráeme algo dulce y quizá te cuente un secreto."));
        L("tail",
                a("¿Mis colas? Son muy suaves... p-puedes tocarlas, si quieres.", "Mis orejas lo oyen todo... incluso tu corazón."),
                a("¡No toques mis colas sin permiso! ...Bueno, solo un poquito.", "Mis colas son las más esponjosas del bosque. Es un hecho."),
                a("*mueve las colas* ¡Mira, mira! ¡Se mueven solas cuando estoy feliz!", "¿Quieres usar mi cola de almohada? ~♥"),
                a("Cada cola guarda un siglo de secretos... o eso dicen.", "Fufu, ¿te gustan? Puedes acariciarlas... si te portas bien."));
        L("howareyou",
                a("Estoy bien... mejor ahora que estás aquí.", "Un poco nerviosa, pero feliz."),
                a("¡Perfectamente! Como siempre. ¿Y tú?", "Aburrida... hasta que llegaste. N-no es un cumplido."),
                a("¡Súper bien! ¡El sol brilla y estás aquí! ~♪", "¡Feliz, feliz, feliz!"),
                a("Tranquila... como la luna sobre el agua.", "Mejor ahora. Fufu."));
        L("bye",
                a("Cuídate mucho, {p}... vuelve pronto.", "*se despide con la cola* Adiós..."),
                a("¿Ya te vas? ...Bien, vete. Pero vuelve, ¿eh?", "Hmph. No tardes."),
                a("¡Adiós, {p}! ¡Te espero! ~♥", "¡Vuelve prontooo!"),
                a("Hasta la próxima luna, {p}.", "Te estaré observando... fufu."));
        L("generic_far",
                a("Mm... ya veo.", "*inclina la cabeza, curiosa*", "N-no estoy segura de qué decir..."),
                a("¿Ajá? ¿Y eso qué?", "Hmph, interesante... supongo.", "Eres un humano raro, ¿lo sabías?"),
                a("¡Jiji, qué divertido!", "¡Cuéntame más! ~♪", "¿En serio? ¡Wow!"),
                a("Fufu... qué curioso.", "Continúa, te escucho.", "Los humanos dicen cosas extrañas."));
        L("generic_close",
                a("Me gusta hablar contigo, {p}...", "*se acerca un poco más* Sigue hablando, me gusta tu voz.", "Contigo me siento tranquila."),
                a("Tonto... pero eres mi tonto.", "¡Hmph! Bueno, tienes razón. Esta vez.", "*te da un golpecito con la cola* Sigue."),
                a("¡Te adoro, {p}! ~♥", "*se acurruca contra ti* Cuéntamelo todo.", "¡Eres el mejor!"),
                a("Fufu, siempre sabes qué decir.", "*juega con tu cabello* ¿Y luego?", "Me encanta escucharte a la luz de la luna."));

        L("gift_love",
                a("¡¿Para mí?! *sus colas se agitan* G-gracias, {p}...", "Es perfecto... lo guardaré en mi corazón."),
                a("¡Wah! ¡Esto es lo mejor! ...Ejem. Gracias.", "¡N-no estoy feliz! ...Bueno, un poquito. ¡Gracias!"),
                a("¡KYAAA! ¡Te quiero! ¡Gracias, gracias! ~♥", "*te abraza muy fuerte* ¡Es el mejor regalo!"),
                a("Fufu... sabes exactamente lo que me gusta.", "Qué detalle tan perfecto. Me tienes hechizada."));
        L("gift_like",
                a("Gracias... es muy amable de tu parte.", "Oh... qué bonito."),
                a("Hmph, lo aceptaré.", "No está mal, humano."),
                a("¡Gracias! ¡Qué lindo! ~♪", "¡Un regalo! ¡Yay!"),
                a("Gracias, {p}. Lo apreciaré.", "Fufu, un detalle encantador."));
        L("gift_hate",
                a("¿E-esto? *retrocede* No me gusta...", "Ugh... ¿por qué me darías esto?"),
                a("¡¿Qué es esta porquería?! ¡Idiota!", "¡Hmph! ¿Es una broma?"),
                a("¡Iuuu! ¡Eso no! ¡Qué asco!", "Aww... no me gusta eso..."),
                a("...Qué gusto tan peculiar tienes.", "Fufu. No vuelvas a hacerlo."));
        L("gift_wait",
                a("Ya me diste algo hace poco... no hace falta tanto.", "Todavía estoy disfrutando tu último regalo."),
                a("¡Calma! Uno a la vez.", "¿Intentas comprarme? Espera un poco."),
                a("¡Espera, espera! ¡Aún no termino el anterior!", "¡Jiji, me consientes demasiado!"),
                a("Paciencia, humano. Saborear es un arte.", "Fufu, más tarde."));
        L("date_low",
                a("¿U-una cita? Todavía no te conozco lo suficiente...", "Quizás más adelante..."),
                a("¡¿Una cita?! ¡Ni lo sueñes! ...Todavía.", "Gánate mi confianza primero."),
                a("¡Primero seamos amigos! ¡Luego vemos! ~♪", "Mmm, aún no... ¡pero pronto!"),
                a("Fufu, qué impaciente. Conóceme primero.", "Aún no me has ganado, humano."));
        L("date_start",
                a("U-una cita... *se arregla el cabello* Vamos, {p}. Te seguiré.", "Caminaré a tu lado... no te alejes."),
                a("¡Bien! Pero me llevas a un lugar bonito. ¡Vamos!", "Una cita contigo... no te emociones. *sonríe*"),
                a("¡Yaaay, una cita! ¡Vamos a pasear juntos! ~♥", "¡Llévame a ver flores, o el mar, o todo!"),
                a("Guíame, {p}. Esta noche soy toda tuya.", "Fufu... muéstrame tu mundo."));
        L("date_chat",
                a("*camina a tu lado, tomando tu mano tímidamente*", "Qué bonito es este lugar contigo..."),
                a("*te mira de reojo* ...Esto es agradable. Solo un poco.", "¡Eh, no camines tan rápido!"),
                a("¡Mira, mira! ¡Qué bonito! ~♪", "*tararea feliz* ¡Me encanta salir contigo!"),
                a("*la luz se refleja en sus ojos* Me gusta este momento.", "Fufu, caminamos como una pareja, ¿no crees?"));
        L("date_good",
                a("Fue una cita maravillosa, {p}... gracias. *te besa la mejilla*", "Nunca olvidaré este día."),
                a("...Fue divertido. ¡Repitámoslo pronto! Es una orden.", "Bien hecho, {p}. Te has ganado otra cita."),
                a("¡La mejor cita del mundo! ¡Te quiero! ~♥", "¡Otra, otra! ¡Quiero otra cita pronto!"),
                a("Una velada perfecta... me robaste un trocito del corazón.", "Fufu, me has impresionado, {p}."));
        L("date_bad",
                a("Me dejaste sola casi toda la cita... *orejas caídas*", "Pensé que querías estar conmigo..."),
                a("¡¿Me abandonaste en nuestra cita?! ¡Imperdonable!", "Hmph. La peor cita de la historia."),
                a("Aww... casi no estuvimos juntos...", "La próxima vez quédate conmigo, ¿sí?"),
                a("Qué decepción, {p}. Una kitsune no olvida.", "Me dejaste esperando bajo la luna..."));
        L("confess_need",
                a("Y-yo... todavía no estoy lista. Salgamos un poco más...", "Necesito conocerte mejor primero..."),
                a("¡¿Novia?! ¡Primero demuéstrame que lo vales!", "Ja, sigue intentándolo."),
                a("¡Aún no! ¡Primero más citas! ~♪", "Mmm... ¡pregúntame otra vez después de más citas!"),
                a("Fufu, todavía no. Tendrás que conquistarme más.", "Paciencia, mi querido humano."));
        L("confess_yes",
                a("¡S-sí! ¡Sí quiero ser tu novia, {p}! *llora de felicidad*"),
                a("...¡Claro que sí, tonto! ¡Tardaste demasiado! *te abraza fuerte*"),
                a("¡SÍÍÍ! ¡Soy tu novia! ¡Te amo, {p}! ~♥♥♥"),
                a("Acepto... desde hoy, eres mío. Fufu. *te besa la frente*"));
        L("jealous",
                a("*mira hacia otro lado* ...A mí también me gustaría un regalo...", "*hace un puchero en silencio*"),
                a("¡Hmph! ¿Y para mí nada? ¡Soy tu novia también!", "¡Oye! ¡Yo estoy aquí, ¿sabes?!"),
                a("¡Oye! ¡Yo también quiero! ~", "¿Y mi abrazo? ¡Exijo un abrazo!"),
                a("Fufu... ¿y mi regalo, querido?", "Qué generoso... con otras."));
        L("hurt",
                a("¡Ay! ¿P-por qué...?"), a("¡OYE! ¡¿Qué te pasa?!"), a("¡Auu! ¡Eso dolió!"), a("...No vuelvas a hacer eso."));
        L("follow_on",
                a("Te seguiré a donde vayas."), a("Bien, te acompaño. Alguien tiene que cuidarte."),
                a("¡Vamos juntos! ~♪"), a("Guía el camino."));
        L("follow_off",
                a("Te esperaré aquí..."), a("Me quedo. ¡No tardes!"), a("¡Te espero aquí!"), a("Aquí estaré, bajo la luna."));
        L("breakup",
                a("...Entiendo. *lágrimas* Fue bonito mientras duró."), a("¡Bien! ¡Vete! ...*se le quiebra la voz*"),
                a("¿Q-qué...? Pero yo te quería... *llora*"), a("Ya veo. Una kitsune nunca olvida, {p}."));
    }

    public static String line(KitsuneEntity k, ServerPlayer p, String key) {
        String[][] all = LINES.get(key);
        if (all == null) return "...";
        String[] opts = all[k.getPersonality().ordinal() % all.length];
        String s = opts[ThreadLocalRandom.current().nextInt(opts.length)];
        return s.replace("{p}", p.getGameProfile().getName());
    }

    public static String greeting(KitsuneEntity k, ServerPlayer p) {
        return line(k, p, "greet_" + k.getStatus(p.getUUID()).ordinal());
    }

    private static String norm(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private static boolean has(String m, String... words) {
        for (String w : words) if (m.contains(w)) return true;
        return false;
    }

    /** Respuesta por palabras clave cuando el chat con IA está desactivado o falla. */
    public static String reply(KitsuneEntity k, ServerPlayer p, String msg) {
        String m = norm(msg);
        RelationStatus st = k.getStatus(p.getUUID());
        String key;
        if (has(m, "te amo", "te quiero", "me gustas", "te adoro", "enamorad")) {
            key = st == RelationStatus.GIRLFRIEND ? "love_gf" : (k.getAffection(p.getUUID()) >= 50 ? "love_mid" : "love_low");
        } else if (has(m, "bonita", "hermosa", "linda", "guapa", "preciosa", "bella", "cute")) key = "compliment";
        else if (has(m, "harem", "otras chicas", "otras novias", "celos", "celosa")) key = "harem";
        else if (has(m, "cita", "salir conmigo", "salimos")) key = "date_talk";
        else if (has(m, "como te llamas", "tu nombre", "quien eres")) key = "name";
        else if (has(m, "edad", "cuantos anos", "anos tienes")) key = "age";
        else if (has(m, "hambre", "comida", "comer", "tofu", "inari", "dulce")) key = "food";
        else if (has(m, "cola", "oreja", "zorro", "kitsune")) key = "tail";
        else if (has(m, "como estas", "que tal", "como te va", "como te sientes")) key = "howareyou";
        else if (has(m, "adios", "chao", "nos vemos", "hasta luego", "bye")) key = "bye";
        else if (has(m, "hola", "buenas", "hey", "saludos", "holi")) key = "greet_" + st.ordinal();
        else key = st.ordinal() >= RelationStatus.DATING.ordinal() ? "generic_close" : "generic_far";
        return line(k, p, key);
    }
}
