package com.idea3d.juegoparaparejas.game

import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import kotlin.random.Random

/**
 * Un desafío a distancia: el pack, las preguntas elegidas y lo que respondió quien desafía.
 * Si [guesses] no es null, es un link de resultado (quien adivinó le devuelve sus respuestas).
 */
data class Challenge(
    val packId: Int,
    val questionIds: List<Int>,
    val answers: List<Int>,
    val fromName: String,
    val toName: String,
    val guesses: List<Int>? = null,
)

/**
 * Codifica un [Challenge] en un texto corto apto para URL (base64url sin padding).
 *
 * Formato v1:
 *   [version][flags][packId][n][nonce hi][nonce lo] + XOR(body) + [crc16 hi][crc16 lo]
 *   body = n ids de pregunta (1 byte c/u) + respuestas (2 bits c/u)
 *          + [adivinanzas (2 bits c/u) si flags&1] + nombre de quien desafía + nombre de quien adivina
 *
 * El XOR solo evita que las respuestas se lean a simple vista en el link; no es cifrado.
 */
object ChallengeCodec {

    private const val VERSION = 1
    private const val FLAG_GUESSES = 0x01
    private const val HEADER_SIZE = 6
    const val MAX_QUESTIONS = 30
    const val MAX_NAME_BYTES = 24

    fun encode(challenge: Challenge, nonce: Int = Random.nextInt(0, 0x10000)): String {
        val n = challenge.questionIds.size
        val guesses = challenge.guesses
        require(n in 1..MAX_QUESTIONS) { "cantidad de preguntas inválida: $n" }
        require(challenge.answers.size == n) { "respuestas y preguntas no coinciden" }
        require(guesses == null || guesses.size == n) { "adivinanzas y preguntas no coinciden" }
        require(challenge.packId in 0..255) { "packId fuera de rango" }
        require(challenge.questionIds.all { it in 0..255 }) { "id de pregunta fuera de rango" }
        require(challenge.answers.all { it in 0..3 }) { "respuesta fuera de rango" }
        require(guesses == null || guesses.all { it in 0..3 }) { "adivinanza fuera de rango" }

        val body = ByteArrayOutputStream()
        challenge.questionIds.forEach { body.write(it) }
        body.write(packTwoBit(challenge.answers))
        if (guesses != null) body.write(packTwoBit(guesses))
        writeName(body, challenge.fromName)
        writeName(body, challenge.toName)

        val safeNonce = nonce and 0xFFFF
        val flags = if (guesses != null) FLAG_GUESSES else 0
        val header = byteArrayOf(
            VERSION.toByte(),
            flags.toByte(),
            challenge.packId.toByte(),
            n.toByte(),
            (safeNonce shr 8).toByte(),
            safeNonce.toByte(),
        )
        val payload = header + xor(body.toByteArray(), safeNonce)
        val crc = crc16(payload)
        return Base64Url.encode(payload + byteArrayOf((crc shr 8).toByte(), crc.toByte()))
    }

    /** Devuelve null si el código está incompleto, alterado o es de una versión desconocida. */
    fun decode(code: String): Challenge? = try {
        decodeOrNull(code.trim())
    } catch (e: RuntimeException) {
        null
    }

    private fun decodeOrNull(code: String): Challenge? {
        val bytes = Base64Url.decode(code) ?: return null
        if (bytes.size < HEADER_SIZE + 2) return null
        val payload = bytes.copyOfRange(0, bytes.size - 2)
        val crc = (bytes[bytes.size - 2].u() shl 8) or bytes[bytes.size - 1].u()
        if (crc16(payload) != crc) return null

        if (payload[0].u() != VERSION) return null
        val flags = payload[1].u()
        val packId = payload[2].u()
        val n = payload[3].u()
        if (n !in 1..MAX_QUESTIONS) return null
        val nonce = (payload[4].u() shl 8) or payload[5].u()
        val body = xor(payload.copyOfRange(HEADER_SIZE, payload.size), nonce)

        val reader = Reader(body)
        val ids = List(n) { reader.byte() }
        val packedSize = (n + 3) / 4
        val answers = unpackTwoBit(reader.bytes(packedSize), n)
        val guesses = if ((flags and FLAG_GUESSES) != 0) unpackTwoBit(reader.bytes(packedSize), n) else null
        val from = reader.name()
        val to = reader.name()
        return Challenge(packId, ids, answers, from, to, guesses)
    }

    private class Reader(private val data: ByteArray) {
        private var pos = 0

        fun byte(): Int {
            if (pos >= data.size) throw IllegalArgumentException("código incompleto")
            return data[pos++].u()
        }

        fun bytes(count: Int): ByteArray {
            if (pos + count > data.size) throw IllegalArgumentException("código incompleto")
            return data.copyOfRange(pos, pos + count).also { pos += count }
        }

        fun name(): String {
            val length = byte()
            if (length > MAX_NAME_BYTES) throw IllegalArgumentException("nombre demasiado largo")
            return String(bytes(length), Charsets.UTF_8)
        }
    }

    private fun writeName(out: ByteArrayOutputStream, name: String) {
        val bytes = truncateUtf8(name.trim(), MAX_NAME_BYTES)
        out.write(bytes.size)
        out.write(bytes)
    }

    /** Recorta sin partir caracteres (emojis incluidos). */
    internal fun truncateUtf8(text: String, maxBytes: Int): ByteArray {
        var s = text
        var bytes = s.toByteArray(Charsets.UTF_8)
        while (bytes.size > maxBytes && s.isNotEmpty()) {
            val dropTwo = s.length >= 2 && s[s.length - 1].isLowSurrogate() && s[s.length - 2].isHighSurrogate()
            s = s.substring(0, s.length - if (dropTwo) 2 else 1)
            bytes = s.toByteArray(Charsets.UTF_8)
        }
        return bytes
    }

    private fun packTwoBit(values: List<Int>): ByteArray {
        val out = ByteArray((values.size + 3) / 4)
        values.forEachIndexed { i, v ->
            out[i / 4] = (out[i / 4].toInt() or ((v and 3) shl ((i % 4) * 2))).toByte()
        }
        return out
    }

    private fun unpackTwoBit(bytes: ByteArray, n: Int): List<Int> =
        List(n) { i -> (bytes[i / 4].toInt() shr ((i % 4) * 2)) and 3 }

    private fun xor(data: ByteArray, nonce: Int): ByteArray {
        var state = (nonce.toLong() * 2654435761L + 0x9E3779B9L) and 0x7FFFFFFFL
        return ByteArray(data.size) { i ->
            state = (state * 1103515245L + 12345L) and 0x7FFFFFFFL
            (data[i].toInt() xor ((state shr 16).toInt() and 0xFF)).toByte()
        }
    }

    /** CRC-16/CCITT-FALSE. */
    private fun crc16(data: ByteArray): Int {
        var crc = 0xFFFF
        for (b in data) {
            crc = crc xor (b.u() shl 8)
            repeat(8) {
                crc = if ((crc and 0x8000) != 0) ((crc shl 1) xor 0x1021) and 0xFFFF else (crc shl 1) and 0xFFFF
            }
        }
        return crc
    }

    private fun Byte.u(): Int = toInt() and 0xFF
}

/** Base64 URL-safe sin padding. Propio porque java.util.Base64 requiere API 26 y minSdk es 23. */
object Base64Url {

    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"

    fun encode(data: ByteArray): String {
        val sb = StringBuilder((data.size * 4 + 2) / 3)
        var i = 0
        while (i < data.size) {
            val b0 = data[i].toInt() and 0xFF
            val b1 = if (i + 1 < data.size) data[i + 1].toInt() and 0xFF else -1
            val b2 = if (i + 2 < data.size) data[i + 2].toInt() and 0xFF else -1
            sb.append(ALPHABET[b0 shr 2])
            sb.append(ALPHABET[((b0 and 0x03) shl 4) or (if (b1 >= 0) b1 shr 4 else 0)])
            if (b1 >= 0) sb.append(ALPHABET[((b1 and 0x0F) shl 2) or (if (b2 >= 0) b2 shr 6 else 0)])
            if (b2 >= 0) sb.append(ALPHABET[b2 and 0x3F])
            i += 3
        }
        return sb.toString()
    }

    fun decode(text: String): ByteArray? {
        val clean = text.trimEnd('=')
        if (clean.length % 4 == 1) return null
        val out = ByteArrayOutputStream(clean.length * 3 / 4)
        var buffer = 0
        var bits = 0
        for (c in clean) {
            val value = when (val index = ALPHABET.indexOf(c)) {
                -1 -> when (c) {
                    '+' -> 62
                    '/' -> 63
                    else -> return null
                }
                else -> index
            }
            buffer = ((buffer shl 6) or value) and 0xFFFF
            bits += 6
            if (bits >= 8) {
                bits -= 8
                out.write((buffer shr bits) and 0xFF)
            }
        }
        return out.toByteArray()
    }
}

object ChallengeLinks {

    const val SCHEME = "juegoparejas"
    const val PLAY_URL = "https://play.google.com/store/apps/details?id=com.idea3d.juegoparaparejas"

    fun webLink(host: String, code: String): String = "https://$host/j/#$code"

    /**
     * Extrae el código de:
     *  - https://<host>/j/#<código>   (link que se comparte)
     *  - https://<host>/j/?c=<código>
     *  - juegoparejas://j?c=<código>  (respaldo desde la página web)
     */
    fun codeFromUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        val isOurPath = url.startsWith("$SCHEME://") || url.contains("/j")
        if (!isOurPath) return null
        val fragment = url.substringAfter('#', "")
        if (fragment.isNotEmpty() && looksLikeCode(fragment)) return fragment
        val query = url.substringAfter('?', "").substringBefore('#')
        return queryParam(query, "c")?.takeIf(::looksLikeCode)
    }

    /** El referrer de Play llega como "c=<código>&utm_source=desafio&utm_medium=link". */
    fun codeFromReferrer(referrer: String?): String? {
        if (referrer.isNullOrBlank()) return null
        return queryParam(referrer, "c")?.takeIf(::looksLikeCode)
    }

    private fun queryParam(query: String, key: String): String? =
        query.split('&')
            .map { it.split('=', limit = 2) }
            .firstOrNull { it.size == 2 && it[0] == key }
            ?.get(1)
            ?.let { runCatching { URLDecoder.decode(it, "UTF-8") }.getOrNull() }

    private fun looksLikeCode(s: String): Boolean =
        s.length in 8..200 && s.all { it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' || it == '-' || it == '_' }
}
