package com.idea3d.juegoparaparejas.data

import android.content.Context
import org.json.JSONObject
import java.util.Locale

enum class Category { COUPLE, FRIENDS, SPICY }

data class Question(
    val id: Int,
    val text: String,
    val answers: List<String>,
)

data class Pack(
    val id: Int,
    val category: Category,
    val premium: Boolean,
    val emoji: String,
    val name: String,
    val subtitle: String,
    val questions: List<Question>,
) {
    fun question(questionId: Int): Question? = questions.firstOrNull { it.id == questionId }
}

/** Idioma del contenido: español, portugués o inglés (por defecto). */
object ContentLanguage {
    fun current(locale: Locale = Locale.getDefault()): String = when (locale.language) {
        "es" -> "es"
        "pt" -> "pt"
        else -> "en"
    }
}

/** Lee assets/packs.json. Sin dependencias de Android para poder testearlo en la JVM. */
object PackParser {

    fun parse(json: String, lang: String): List<Pack> {
        val root = JSONObject(json)
        val packs = root.getJSONArray("packs")
        return (0 until packs.length()).map { i ->
            val p = packs.getJSONObject(i)
            val questions = p.getJSONArray("questions")
            Pack(
                id = p.getInt("id"),
                category = Category.valueOf(p.getString("category").uppercase(Locale.ROOT)),
                premium = p.optBoolean("premium", false),
                emoji = p.optString("emoji", "❤"),
                name = p.getJSONObject("name").localized(lang),
                subtitle = p.optJSONObject("subtitle")?.localized(lang).orEmpty(),
                questions = (0 until questions.length()).map { j ->
                    val q = questions.getJSONObject(j)
                    val answers = q.getJSONArray("a")
                    Question(
                        id = q.getInt("id"),
                        text = q.getJSONObject("q").localized(lang),
                        answers = (0 until answers.length()).map { k ->
                            answers.getJSONObject(k).localized(lang)
                        },
                    )
                },
            )
        }
    }

    private fun JSONObject.localized(lang: String): String {
        for (key in listOf(lang, "es", "en")) {
            val value = optString(key, "")
            if (value.isNotBlank()) return value
        }
        return ""
    }
}

class PackRepository(private val context: Context) {

    val packs: List<Pack> by lazy {
        val json = context.assets.open("packs.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        PackParser.parse(json, ContentLanguage.current())
    }

    fun pack(id: Int): Pack? = packs.firstOrNull { it.id == id }
}
