package com.idea3d.juegoparaparejas.game

import com.idea3d.juegoparaparejas.data.Pack
import kotlin.random.Random

enum class Mode { LOCAL, REMOTE }

enum class Phase { ANSWER, GUESS }

object GameEngine {

    const val QUESTIONS_PER_ROUND = 10

    /** Elige preguntas al azar del pack para que cada ronda sea distinta. */
    fun pickQuestions(pack: Pack, random: Random = Random.Default, count: Int = QUESTIONS_PER_ROUND): List<Int> =
        pack.questions.map { it.id }.shuffled(random).take(minOf(count, pack.questions.size))

    fun score(answers: List<Int>, guesses: List<Int>): Int =
        answers.zip(guesses).count { (answer, guess) -> answer == guess }

    /** Porcentaje redondeado al entero más cercano. */
    fun percent(score: Int, total: Int): Int =
        if (total <= 0) 0 else ((score * 100.0) / total + 0.5).toInt().coerceIn(0, 100)

    /** 0: recién se conocen · 1: buen camino · 2: se conocen bien · 3: almas gemelas */
    fun tier(percent: Int): Int = when {
        percent >= 90 -> 3
        percent >= 60 -> 2
        percent >= 30 -> 1
        else -> 0
    }
}
