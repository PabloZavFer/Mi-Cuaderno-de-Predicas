package com.pablozavfer.cuadernopredicas.data

import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonPoint
import com.pablozavfer.cuadernopredicas.data.model.SermonStatus
import java.time.LocalDate

/**
 * Tres ejemplos breves y originales que enseñan a usar el editor.
 * No citan traducciones bíblicas con derechos de autor: solo dan referencias y explicaciones propias.
 * Los uid son fijos para poder volver a agregarlos sin duplicarlos.
 */
object SampleContent {
    const val SAMPLE_TAG = "Ejemplo"
    private const val NOTE = "Este es un ejemplo editable. Muestra cómo llenar cada sección: " +
        "puedes modificarlo, duplicarlo para usarlo como base o eliminarlo."

    fun build(today: LocalDate): List<Sermon> = listOf(
        Sermon(
            uid = "ejemplo-expositiva",
            isSample = true,
            title = "Confianza en el Buen Pastor",
            scripture = "Salmo 23",
            preachDate = today.plusDays(7),
            status = SermonStatus.READY,
            tags = listOf(SAMPLE_TAG, "Expositiva"),
            context = "Salmo atribuido a David, quien conoció de joven el oficio de pastor. " +
                "El salmo usa dos imágenes: el pastor que cuida a su rebaño y el anfitrión " +
                "que recibe a su invitado.",
            introduction = "Todos atravesamos temporadas de incertidumbre. Este salmo no promete " +
                "una vida sin valles, pero sí una compañía fiel en medio de ellos.",
            points = listOf(
                SermonPoint(
                    title = "El Pastor provee (v. 1-3)",
                    body = "Dios conoce nuestras necesidades y nos guía a lugares de descanso. " +
                        "Su dirección tiene un propósito: honrar su propio nombre.",
                ),
                SermonPoint(
                    title = "El Pastor acompaña (v. 4)",
                    body = "En el valle oscuro el salmista deja de hablar acerca de Dios y comienza " +
                        "a hablarle directamente. La presencia de Dios es más fuerte que el temor.",
                ),
                SermonPoint(
                    title = "El Pastor recibe (v. 5-6)",
                    body = "La imagen cambia a un banquete. Dios no solo protege: también honra " +
                        "y acoge. Su bondad nos acompaña todos los días.",
                ),
            ),
            application = "Identifica un área en la que hoy sientes temor o necesidad. Escribe una " +
                "oración breve para entregarla al cuidado del Buen Pastor.",
            conclusion = "El mismo Pastor que guió a David nos guía hoy. Jesús se presentó como el " +
                "Buen Pastor que da su vida por sus ovejas (Juan 10:11).",
            notes = NOTE,
        ),
        Sermon(
            uid = "ejemplo-tematica",
            isSample = true,
            title = "Gratitud en todo tiempo",
            scripture = "1 Tesalonicenses 5:16-18; Filipenses 4:6-7; Salmo 100",
            status = SermonStatus.DRAFT,
            tags = listOf(SAMPLE_TAG, "Temática"),
            context = "La gratitud aparece en toda la Biblia como una respuesta a quién es Dios, " +
                "no solo a lo que recibimos. Pablo escribe sobre ella incluso desde la prisión.",
            introduction = "¿Es posible dar gracias cuando las cosas no salen como esperábamos? " +
                "Hoy veremos que la gratitud cristiana no depende de las circunstancias.",
            points = listOf(
                SermonPoint(
                    title = "Gratitud como estilo de vida (1 Tesalonicenses 5:16-18)",
                    body = "Pablo une la alegría, la oración y la gratitud. No son emociones " +
                        "pasajeras, sino hábitos que se cultivan.",
                ),
                SermonPoint(
                    title = "Gratitud que vence la ansiedad (Filipenses 4:6-7)",
                    body = "Presentar nuestras peticiones con acción de gracias cambia el enfoque: " +
                        "recordamos la fidelidad de Dios mientras esperamos su respuesta.",
                ),
                SermonPoint(
                    title = "Gratitud que se hace alabanza (Salmo 100)",
                    body = "La gratitud personal se vuelve celebración del pueblo de Dios reunido.",
                ),
            ),
            application = "Durante esta semana, anota cada noche tres motivos de gratitud y " +
                "compártelos con alguien.",
            conclusion = "Dar gracias es reconocer que Dios sigue siendo bueno en todo tiempo.",
            notes = NOTE,
        ),
        Sermon(
            uid = "ejemplo-grupo",
            isSample = true,
            title = "Una comunidad que se cuida",
            scripture = "Hechos 2:42-47",
            preachDate = today.plusDays(3),
            status = SermonStatus.READY,
            tags = listOf(SAMPLE_TAG, "Grupo pequeño"),
            context = "Lucas describe la vida de la primera iglesia en Jerusalén, poco después " +
                "de Pentecostés. Eran personas nuevas en la fe aprendiendo a vivir juntas.",
            introduction = "Para romper el hielo: ¿qué persona te ha hecho sentir parte de una " +
                "comunidad y qué hizo para lograrlo?",
            points = listOf(
                SermonPoint(
                    title = "Leer juntos",
                    body = "Lean el pasaje en voz alta. Pregunta: ¿qué actividades practicaba " +
                        "esta comunidad?",
                ),
                SermonPoint(
                    title = "Entender",
                    body = "¿Por qué crees que compartían sus bienes y sus comidas? ¿Qué " +
                        "resultado tuvo su manera de vivir en quienes los rodeaban?",
                ),
                SermonPoint(
                    title = "Aplicar",
                    body = "¿Qué práctica de este pasaje podemos fortalecer como grupo durante " +
                        "este mes?",
                ),
            ),
            application = "Oren en parejas por una necesidad concreta y acuerden cómo apoyarse " +
                "durante la semana.",
            conclusion = "Una comunidad que se cuida refleja el amor de Cristo a quienes todavía " +
                "no lo conocen.",
            notes = NOTE,
        ),
    )
}
