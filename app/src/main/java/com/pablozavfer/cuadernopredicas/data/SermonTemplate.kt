package com.pablozavfer.cuadernopredicas.data

import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonPoint
import java.time.LocalDate

/**
 * Plantillas para empezar una prédica. Las indicaciones se escriben dentro de los campos
 * para que el usuario las lea, las reemplace o las borre.
 */
enum class SermonTemplate(val label: String, val description: String) {
    BLANK("En blanco", "Todas las secciones vacías."),
    EXPOSITORY("Prédica expositiva", "Explica un pasaje versículo por versículo y lo aplica hoy."),
    TOPICAL("Prédica temática", "Desarrolla un tema con varios pasajes de la Biblia."),
    SMALL_GROUP("Estudio para grupo pequeño", "Lectura, preguntas y oración para conversar en grupo.");

    fun build(preachDate: LocalDate? = null): Sermon = when (this) {
        BLANK -> Sermon(preachDate = preachDate, points = listOf(SermonPoint()))

        EXPOSITORY -> Sermon(
            preachDate = preachDate,
            tags = listOf("Expositiva"),
            context = "Indicación: ¿quién escribió el pasaje, a quién, cuándo y por qué? " +
                "Anota lo que ocurre antes y después del texto.",
            introduction = "Indicación: presenta la idea central del pasaje en una o dos frases " +
                "y conéctala con una necesidad real de tus oyentes.",
            points = listOf(
                SermonPoint(
                    title = "Lo que dice el texto",
                    body = "Indicación: explica los versículos en orden. ¿Qué palabras o ideas se repiten?",
                ),
                SermonPoint(
                    title = "Lo que significaba",
                    body = "Indicación: ¿qué entendieron los primeros lectores? Apóyate en el contexto.",
                ),
                SermonPoint(
                    title = "La verdad para hoy",
                    body = "Indicación: ¿qué enseña el pasaje acerca de Dios y acerca de nosotros?",
                ),
            ),
            application = "Indicación: propone uno o dos pasos concretos para esta semana.",
            conclusion = "Indicación: resume la idea central y termina con una invitación clara " +
                "a responder a la Palabra.",
            notes = "Indicación: preguntas pendientes, ilustraciones y referencias cruzadas.",
        )

        TOPICAL -> Sermon(
            preachDate = preachDate,
            tags = listOf("Temática"),
            context = "Indicación: define el tema y explica por qué es importante para tu " +
                "congregación ahora. Reúne los pasajes principales que lo tratan.",
            introduction = "Indicación: abre con una pregunta, una situación cotidiana o una " +
                "ilustración que despierte interés por el tema.",
            points = listOf(
                SermonPoint(
                    title = "Primer aspecto del tema",
                    body = "Indicación: escribe la idea y el pasaje que la respalda.",
                ),
                SermonPoint(
                    title = "Segundo aspecto del tema",
                    body = "Indicación: muestra otra faceta del tema con su pasaje.",
                ),
                SermonPoint(
                    title = "Tercer aspecto del tema",
                    body = "Indicación: cierra el desarrollo con la enseñanza más práctica.",
                ),
            ),
            application = "Indicación: ¿cómo cambia este tema la vida diaria? Da ejemplos concretos.",
            conclusion = "Indicación: une los aspectos en una frase que se pueda recordar.",
            notes = "Indicación: lista de pasajes consultados y recursos.",
        )

        SMALL_GROUP -> Sermon(
            preachDate = preachDate,
            tags = listOf("Grupo pequeño"),
            context = "Indicación: resume en pocas palabras el trasfondo del pasaje para que " +
                "todos lo entiendan antes de leer.",
            introduction = "Indicación: pregunta para romper el hielo, fácil de responder por todos.",
            points = listOf(
                SermonPoint(
                    title = "Leer juntos",
                    body = "Indicación: pide a alguien que lea el pasaje en voz alta. " +
                        "Pregunta: ¿qué te llamó la atención?",
                ),
                SermonPoint(
                    title = "Entender",
                    body = "Indicación: dos o tres preguntas sobre lo que el texto dice y enseña.",
                ),
                SermonPoint(
                    title = "Aplicar",
                    body = "Indicación: ¿qué haremos diferente esta semana a partir de lo que leímos?",
                ),
            ),
            application = "Indicación: tiempo de oración en parejas o en grupo.",
            conclusion = "Indicación: frase de cierre y anuncio del próximo encuentro.",
            notes = "Indicación: quién dirige, materiales y peticiones de oración.",
        )
    }

    companion object {
        fun fromKey(key: String?): SermonTemplate = entries.firstOrNull { it.name == key } ?: BLANK
    }
}
