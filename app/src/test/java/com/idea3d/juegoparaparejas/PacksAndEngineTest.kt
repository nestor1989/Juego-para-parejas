package com.idea3d.juegoparaparejas

import com.idea3d.juegoparaparejas.data.Category
import com.idea3d.juegoparaparejas.data.PackParser
import com.idea3d.juegoparaparejas.game.GameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

class PacksAndEngineTest {

    private val json = File("src/main/assets/packs.json").readText(Charsets.UTF_8)

    @Test
    fun everyPackIsPlayableInEveryLanguage() {
        for (lang in listOf("es", "en", "pt")) {
            val packs = PackParser.parse(json, lang)
            assertTrue(packs.isNotEmpty())
            assertEquals(packs.size, packs.map { it.id }.toSet().size)
            for (pack in packs) {
                assertTrue("${pack.id} sin nombre ($lang)", pack.name.isNotBlank())
                assertTrue("${pack.id} tiene pocas preguntas", pack.questions.size >= GameEngine.QUESTIONS_PER_ROUND)
                assertEquals(pack.questions.size, pack.questions.map { it.id }.toSet().size)
                for (q in pack.questions) {
                    assertTrue("pregunta ${pack.id}/${q.id} vacía ($lang)", q.text.isNotBlank())
                    assertEquals(4, q.answers.size)
                    assertTrue(q.answers.all { it.isNotBlank() })
                    assertTrue(q.id in 0..255)
                }
            }
        }
    }

    @Test
    fun catalogHasEveryCategoryAndAFreeSpicyPack() {
        val packs = PackParser.parse(json, "es")
        for (category in Category.values()) {
            assertTrue("falta $category", packs.any { it.category == category })
        }
        assertTrue(packs.filter { it.category != Category.SPICY }.none { it.premium })
        assertTrue(packs.any { it.category == Category.SPICY && !it.premium })
    }

    @Test
    fun noMinorAgesInSpicyContent() {
        val forbidden = Regex("\\b(1[0-7])\\b|virgin|virgindad|colegiala|schoolgirl", RegexOption.IGNORE_CASE)
        for (lang in listOf("es", "en", "pt")) {
            PackParser.parse(json, lang).filter { it.category == Category.SPICY }.forEach { pack ->
                pack.questions.forEach { q ->
                    (listOf(q.text) + q.answers).forEach { text ->
                        assertFalse("contenido no permitido en ${pack.id}/${q.id}: $text", forbidden.containsMatchIn(text))
                    }
                }
            }
        }
    }

    @Test
    fun pickQuestionsIsRandomUniqueAndSized() {
        val pack = PackParser.parse(json, "es").first()
        val picked = GameEngine.pickQuestions(pack, Random(1))
        assertEquals(GameEngine.QUESTIONS_PER_ROUND, picked.size)
        assertEquals(picked.size, picked.toSet().size)
        assertTrue(picked.all { id -> pack.question(id) != null })
    }

    @Test
    fun scoreAndPercent() {
        assertEquals(3, GameEngine.score(listOf(0, 1, 2, 3), listOf(0, 1, 2, 0)))
        assertEquals(75, GameEngine.percent(3, 4))
        assertEquals(67, GameEngine.percent(2, 3))
        assertEquals(0, GameEngine.percent(0, 0))
        assertEquals(100, GameEngine.percent(10, 10))
        assertEquals(3, GameEngine.tier(90))
        assertEquals(2, GameEngine.tier(60))
        assertEquals(1, GameEngine.tier(30))
        assertEquals(0, GameEngine.tier(29))
    }
}
