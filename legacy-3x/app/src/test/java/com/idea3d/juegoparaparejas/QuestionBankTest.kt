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
                assertEquals(4, question.answerRes.size)
                assert(question.questionRes != 0)
                question.answerRes.forEach { assert(it != 0) }
            }
        }
    }
    @Test
    fun spotChecksResourceIdsAreDistinct() {
        val p1q0 = QuestionBank.getPack(1).questionAt(0)
        val p1q1 = QuestionBank.getPack(1).questionAt(1)
        assert(p1q0.questionRes != p1q1.questionRes) { "Adjacent questions should have different resource IDs" }
        val p1 = QuestionBank.getPack(1)
        val p2 = QuestionBank.getPack(2)
        assert(p1.questionAt(0).questionRes != p2.questionAt(0).questionRes) { "Different packs should have different resource IDs" }
    }
}
