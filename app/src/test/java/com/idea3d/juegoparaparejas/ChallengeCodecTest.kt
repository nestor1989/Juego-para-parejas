package com.idea3d.juegoparaparejas

import com.idea3d.juegoparaparejas.game.Base64Url
import com.idea3d.juegoparaparejas.game.Challenge
import com.idea3d.juegoparaparejas.game.ChallengeCodec
import com.idea3d.juegoparaparejas.game.ChallengeLinks
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ChallengeCodecTest {

    private val sample = Challenge(
        packId = 3,
        questionIds = listOf(1, 5, 9, 12, 2, 7, 3, 11, 4, 8),
        answers = listOf(0, 1, 2, 3, 3, 2, 1, 0, 1, 2),
        fromName = "Ana",
        toName = "Néstor",
    )

    @Test
    fun roundTrip() {
        val code = ChallengeCodec.encode(sample, nonce = 1234)
        assertEquals(sample, ChallengeCodec.decode(code))
    }

    @Test
    fun roundTripWithGuessesAndEmoji() {
        val result = sample.copy(fromName = "Sofi 💖", toName = "Juanma", guesses = listOf(0, 0, 2, 3, 1, 2, 1, 0, 3, 2))
        val code = ChallengeCodec.encode(result)
        assertEquals(result, ChallengeCodec.decode(code))
    }

    @Test
    fun randomRoundTrips() {
        val random = Random(42)
        repeat(500) {
            val n = random.nextInt(1, ChallengeCodec.MAX_QUESTIONS + 1)
            val c = Challenge(
                packId = random.nextInt(0, 256),
                questionIds = List(n) { random.nextInt(0, 256) },
                answers = List(n) { random.nextInt(0, 4) },
                fromName = "A".repeat(random.nextInt(0, 10)),
                toName = "B".repeat(random.nextInt(0, 10)),
                guesses = if (random.nextBoolean()) List(n) { random.nextInt(0, 4) } else null,
            )
            assertEquals(c, ChallengeCodec.decode(ChallengeCodec.encode(c, random.nextInt(0, 0x10000))))
        }
    }

    @Test
    fun codeIsUrlSafeAndShort() {
        val code = ChallengeCodec.encode(sample)
        assertTrue(code.all { it.isLetterOrDigit() || it == '-' || it == '_' })
        assertTrue("código demasiado largo: ${code.length}", code.length < 60)
    }

    @Test
    fun answersAreNotVisibleAcrossNonces() {
        assertNotEquals(ChallengeCodec.encode(sample, nonce = 1), ChallengeCodec.encode(sample, nonce = 2))
    }

    @Test
    fun tamperedOrTruncatedCodesAreRejected() {
        val code = ChallengeCodec.encode(sample, nonce = 99)
        val flipped = (if (code[10] == 'A') 'B' else 'A').toString()
        assertNull(ChallengeCodec.decode(code.substring(0, 10) + flipped + code.substring(11)))
        assertNull(ChallengeCodec.decode(code.dropLast(3)))
        assertNull(ChallengeCodec.decode(""))
        assertNull(ChallengeCodec.decode("hola!"))
    }

    @Test
    fun longNamesAreTruncatedWithoutBreakingEmoji() {
        val bytes = ChallengeCodec.truncateUtf8("Mariana 😍😍😍😍😍😍", ChallengeCodec.MAX_NAME_BYTES)
        assertTrue(bytes.size <= ChallengeCodec.MAX_NAME_BYTES)
        val text = String(bytes, Charsets.UTF_8)
        assertTrue(text.startsWith("Mariana"))
        assertTrue(text.none { it == '�' })
    }

    @Test
    fun base64MatchesKnownVectors() {
        assertEquals("", Base64Url.encode(ByteArray(0)))
        assertEquals("Zg", Base64Url.encode("f".toByteArray()))
        assertEquals("Zm8", Base64Url.encode("fo".toByteArray()))
        assertEquals("Zm9v", Base64Url.encode("foo".toByteArray()))
        assertEquals("Zm9vYmFy", Base64Url.encode("foobar".toByteArray()))
        assertEquals("-_8", Base64Url.encode(byteArrayOf(0xFB.toByte(), 0xFF.toByte())))
        assertArrayEquals("foobar".toByteArray(), Base64Url.decode("Zm9vYmFy"))
        assertArrayEquals(byteArrayOf(0xFB.toByte(), 0xFF.toByte()), Base64Url.decode("-_8"))
    }

    @Test
    fun linksAndReferrerAreParsed() {
        val code = ChallengeCodec.encode(sample)
        assertEquals(code, ChallengeLinks.codeFromUrl(ChallengeLinks.webLink("juego-para-parejas-3ea2c.web.app", code)))
        assertEquals(code, ChallengeLinks.codeFromUrl("https://juego-para-parejas-3ea2c.web.app/j/?c=$code"))
        assertEquals(code, ChallengeLinks.codeFromUrl("juegoparejas://j?c=$code"))
        assertEquals(code, ChallengeLinks.codeFromReferrer("c=$code&utm_source=desafio&utm_medium=link"))
        assertEquals(code, ChallengeLinks.codeFromReferrer("utm_source=desafio&c=$code"))
        assertNull(ChallengeLinks.codeFromReferrer("utm_source=google-play&utm_medium=organic"))
        assertNull(ChallengeLinks.codeFromUrl("https://juego-para-parejas-3ea2c.web.app/otra-pagina"))
        assertNull(ChallengeLinks.codeFromUrl(null))
    }
}
