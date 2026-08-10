package com.idea3d.juegoparaparejas
import org.junit.Assert.assertEquals
import org.junit.Test
class QuestionBankTest {
    @Test
    fun packs_haveSixteenEntries() {
        assertEquals(16, QuestionBank.packs.size)
    }
    @Test
    fun everyPackHasTwelveQuestionsAndFourAnswers() {
        QuestionBank.packs.values.forEach { pack ->
            assertEquals(12, pack.questions.size)
            pack.questions.forEach { question ->
                assertEquals(4, question.answers.size)
            }
        }
    }
    @Test
    fun spotChecksSpecificContent() {
        assertEquals("Tu estación favorita es...", QuestionBank.getPack(1).questionAt(0).question)
        assertEquals("1-3", QuestionBank.getPack(11).questionAt(0).answers[0])
        assertEquals("El futuro", QuestionBank.getPack(1).questionAt(6).answers[3])
        assertEquals("Sadomasoquismo", QuestionBank.getPack(16).questionAt(11).answers[3])
    }
}
