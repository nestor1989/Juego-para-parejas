package com.idea3d.juegoparaparejas.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.idea3d.juegoparaparejas.App
import com.idea3d.juegoparaparejas.BuildConfig
import com.idea3d.juegoparaparejas.data.Category
import com.idea3d.juegoparaparejas.data.Pack
import com.idea3d.juegoparaparejas.data.Question
import com.idea3d.juegoparaparejas.game.Challenge
import com.idea3d.juegoparaparejas.game.ChallengeCodec
import com.idea3d.juegoparaparejas.game.ChallengeLinks
import com.idea3d.juegoparaparejas.game.GameEngine
import com.idea3d.juegoparaparejas.game.Mode
import com.idea3d.juegoparaparejas.game.Phase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Home : Screen
    data object HowTo : Screen
    data class Setup(val mode: Mode) : Screen
    data class Packs(val mode: Mode) : Screen
    data class Handoff(val phase: Phase) : Screen
    data class Play(val phase: Phase) : Screen
    data object Result : Screen
    data object ChallengeReady : Screen
    data object ChallengeIntro : Screen
    data object InvalidChallenge : Screen
}

/** LOCAL: mismo celular · RECEIVER: adivinó un desafío · VIEWER: quien desafió mira el resultado. */
enum class ResultKind { LOCAL, RECEIVER, VIEWER }

data class Session(
    val mode: Mode,
    val pack: Pack,
    val questions: List<Question>,
    val answererName: String,
    val guesserName: String,
    val answers: List<Int> = emptyList(),
    val guesses: List<Int> = emptyList(),
    val kind: ResultKind = ResultKind.LOCAL,
) {
    val total: Int get() = questions.size
    val score: Int get() = GameEngine.score(answers, guesses)
    val percent: Int get() = GameEngine.percent(score, total)
}

data class Reveal(val chosen: Int, val correct: Int)

data class UiState(
    val screen: Screen = Screen.Home,
    val session: Session? = null,
    val player1: String = "",
    val player2: String = "",
    val reveal: Reveal? = null,
    val confirmExit: Boolean = false,
    val showPaywall: Boolean = false,
    val paywallPack: Pack? = null,
    val adultGatePack: Pack? = null,
    val challengeLink: String? = null,
    val pendingChallenge: Challenge? = null,
    val unlockedPackIds: Set<Int> = emptySet(),
    val askReview: Boolean = false,
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as App
    private val prefs = app.prefs
    private val analytics = app.analytics

    private val _state = MutableStateFlow(UiState(player1 = prefs.player1, player2 = prefs.player2))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var pendingPackMode: Mode = Mode.LOCAL

    val packs: List<Pack> get() = app.packs.packs

    // ---------- Navegación ----------

    fun goHome() = _state.update {
        it.copy(screen = Screen.Home, session = null, reveal = null, confirmExit = false, pendingChallenge = null)
    }

    fun openHowTo() = _state.update { it.copy(screen = Screen.HowTo) }

    fun openSetup(mode: Mode) = _state.update { it.copy(screen = Screen.Setup(mode)) }

    /** Devuelve false si la app debería cerrarse (atrás en el inicio). */
    fun back(): Boolean {
        val current = _state.value
        when (val screen = current.screen) {
            Screen.Home -> return false
            Screen.HowTo, is Screen.Setup, Screen.ChallengeReady,
            Screen.ChallengeIntro, Screen.InvalidChallenge, Screen.Result -> goHome()
            is Screen.Packs -> _state.update { it.copy(screen = Screen.Setup(screen.mode)) }
            is Screen.Handoff, is Screen.Play -> _state.update { it.copy(confirmExit = true) }
        }
        return true
    }

    fun cancelExit() = _state.update { it.copy(confirmExit = false) }

    fun confirmExit() {
        val mode = _state.value.session?.mode ?: Mode.LOCAL
        _state.update { it.copy(confirmExit = false, session = null, reveal = null, screen = Screen.Packs(mode)) }
    }

    // ---------- Jugadores ----------

    fun setPlayer1(name: String) = _state.update { it.copy(player1 = name.take(24)) }
    fun setPlayer2(name: String) = _state.update { it.copy(player2 = name.take(24)) }

    fun confirmSetup(mode: Mode) {
        val s = _state.value
        if (s.player1.isBlank() || s.player2.isBlank()) return
        prefs.player1 = s.player1.trim()
        prefs.player2 = s.player2.trim()
        _state.update { it.copy(player1 = it.player1.trim(), player2 = it.player2.trim(), screen = Screen.Packs(mode)) }
    }

    // ---------- Packs, puerta +18 y paywall ----------

    fun isLocked(pack: Pack, isPremium: Boolean): Boolean =
        pack.premium && !isPremium && pack.id !in _state.value.unlockedPackIds

    fun selectPack(pack: Pack, mode: Mode) {
        pendingPackMode = mode
        val isPremium = app.billing.isPremium.value
        when {
            pack.category == Category.SPICY && !prefs.adultConfirmed ->
                _state.update { it.copy(adultGatePack = pack) }
            isLocked(pack, isPremium) -> openPaywall(pack)
            else -> startRound(pack, mode)
        }
    }

    fun confirmAdult() {
        val pack = _state.value.adultGatePack ?: return
        prefs.adultConfirmed = true
        _state.update { it.copy(adultGatePack = null) }
        selectPack(pack, pendingPackMode)
    }

    fun dismissAdultGate() = _state.update { it.copy(adultGatePack = null) }

    fun openPaywall(pack: Pack?) {
        analytics.log("paywall_view", "pack" to pack?.id)
        _state.update { it.copy(showPaywall = true, paywallPack = pack) }
    }

    fun dismissPaywall() = _state.update { it.copy(showPaywall = false, paywallPack = null) }

    /** Rewarded visto: el pack queda abierto para esta partida. */
    fun unlockWithAd(pack: Pack) {
        analytics.log("rewarded_unlock", "pack" to pack.id)
        _state.update { it.copy(unlockedPackIds = it.unlockedPackIds + pack.id, showPaywall = false, paywallPack = null) }
        startRound(pack, pendingPackMode)
    }

    fun onPurchaseSuccess() {
        analytics.log("purchase_complete")
        val pack = _state.value.paywallPack
        dismissPaywall()
        if (pack != null) startRound(pack, pendingPackMode)
    }

    // ---------- Ronda ----------

    fun startRound(pack: Pack, mode: Mode) {
        val questions = GameEngine.pickQuestions(pack).mapNotNull { pack.question(it) }
        val s = _state.value
        val session = Session(
            mode = mode,
            pack = pack,
            questions = questions,
            answererName = s.player1,
            guesserName = s.player2,
        )
        analytics.log("round_start", "mode" to mode.name, "pack" to pack.id)
        _state.update {
            it.copy(
                session = session,
                reveal = null,
                challengeLink = null,
                screen = if (mode == Mode.LOCAL) Screen.Handoff(Phase.ANSWER) else Screen.Play(Phase.ANSWER),
            )
        }
    }

    fun handoffReady(phase: Phase) = _state.update { it.copy(screen = Screen.Play(phase)) }

    fun answer(choice: Int) {
        val session = _state.value.session ?: return
        if (session.answers.size >= session.total) return
        val updated = session.copy(answers = session.answers + choice)
        val done = updated.answers.size == updated.total
        when {
            !done -> _state.update { it.copy(session = updated) }
            updated.mode == Mode.LOCAL ->
                _state.update { it.copy(session = updated, screen = Screen.Handoff(Phase.GUESS)) }
            else -> {
                val link = buildChallengeLink(updated)
                _state.update { it.copy(session = updated, challengeLink = link, screen = Screen.ChallengeReady) }
            }
        }
    }

    fun guess(choice: Int) {
        val current = _state.value
        val session = current.session ?: return
        if (current.reveal != null || session.guesses.size >= session.total) return
        if (session.mode == Mode.LOCAL) {
            // En el mismo celular no se muestra si acertó: se avanza al instante, como en la
            // primera ronda, y los aciertos se ven juntos en el repaso del resultado.
            val updated = session.copy(guesses = session.guesses + choice)
            _state.update { it.copy(session = updated) }
            if (updated.guesses.size == updated.total) finishRound(updated)
            return
        }
        // A distancia, quien adivina está solo: ver el acierto en el momento es parte de la gracia.
        val correct = session.answers[session.guesses.size]
        _state.update { it.copy(reveal = Reveal(chosen = choice, correct = correct)) }
        viewModelScope.launch {
            delay(if (choice == correct) 700L else 1200L)
            val latest = _state.value.session ?: return@launch
            val updated = latest.copy(guesses = latest.guesses + choice)
            _state.update { it.copy(session = updated, reveal = null) }
            if (updated.guesses.size == updated.total) finishRound(updated)
        }
    }

    private fun finishRound(session: Session) {
        prefs.roundsCompleted = prefs.roundsCompleted + 1
        app.ads.onRoundCompleted()
        analytics.log(
            "round_complete",
            "mode" to session.mode.name,
            "kind" to session.kind.name,
            "pack" to session.pack.id,
            "percent" to session.percent,
        )
        val shouldAskReview = session.percent >= 60 &&
            prefs.roundsCompleted >= 2 &&
            prefs.reviewAskedVersion != BuildConfig.VERSION_CODE
        if (shouldAskReview) prefs.reviewAskedVersion = BuildConfig.VERSION_CODE
        _state.update { it.copy(screen = Screen.Result, askReview = shouldAskReview) }
    }

    fun consumeReviewRequest() {
        if (_state.value.askReview) analytics.log("review_prompt")
        _state.update { it.copy(askReview = false) }
    }

    // ---------- Acciones del resultado ----------

    fun switchRoles() {
        val s = _state.value.session ?: return
        _state.update {
            it.copy(
                session = s.copy(
                    answererName = s.guesserName,
                    guesserName = s.answererName,
                    answers = emptyList(),
                    guesses = emptyList(),
                ),
                screen = Screen.Handoff(Phase.ANSWER),
            )
        }
    }

    /** Quien adivinó un desafío responde las mismas preguntas y le devuelve el desafío. */
    fun challengeBack() {
        val s = _state.value.session ?: return
        val me = s.guesserName
        val them = s.answererName
        prefs.player1 = me
        prefs.player2 = them
        _state.update {
            it.copy(
                player1 = me,
                player2 = them,
                session = Session(
                    mode = Mode.REMOTE,
                    pack = s.pack,
                    questions = s.questions,
                    answererName = me,
                    guesserName = them,
                ),
                challengeLink = null,
                screen = Screen.Play(Phase.ANSWER),
            )
        }
    }

    /** Elegir otro pack, manteniendo a las mismas personas. */
    fun otherPack() {
        val s = _state.value.session
        if (s == null) {
            _state.update { it.copy(screen = Screen.Packs(Mode.LOCAL)) }
            return
        }
        val unlocked = _state.value.unlockedPackIds - s.pack.id
        // En los modos a distancia, "yo" pasa a ser el jugador 1.
        val (me, them) = when (s.kind) {
            ResultKind.RECEIVER -> s.guesserName to s.answererName
            ResultKind.VIEWER -> s.answererName to s.guesserName
            ResultKind.LOCAL -> _state.value.player1 to _state.value.player2
        }
        prefs.player1 = me
        prefs.player2 = them
        val mode = if (s.kind == ResultKind.LOCAL) s.mode else Mode.REMOTE
        _state.update {
            it.copy(
                player1 = me,
                player2 = them,
                unlockedPackIds = unlocked,
                session = null,
                screen = Screen.Packs(mode),
            )
        }
    }

    /** Link de resultado para que quien desafió vea las respuestas en su app. */
    fun resultLink(): String? {
        val s = _state.value.session ?: return null
        if (s.kind != ResultKind.RECEIVER) return null
        val code = ChallengeCodec.encode(
            Challenge(
                packId = s.pack.id,
                questionIds = s.questions.map { it.id },
                answers = s.answers,
                fromName = s.answererName,
                toName = s.guesserName,
                guesses = s.guesses,
            )
        )
        return ChallengeLinks.webLink(BuildConfig.CHALLENGE_HOST, code)
    }

    fun onResultShared() {
        val s = _state.value.session ?: return
        analytics.log("result_shared", "kind" to s.kind.name, "percent" to s.percent)
    }

    fun onChallengeShared(channel: String) {
        val s = _state.value.session ?: return
        analytics.log("challenge_sent", "pack" to s.pack.id, "channel" to channel)
    }

    private fun buildChallengeLink(session: Session): String {
        val code = ChallengeCodec.encode(
            Challenge(
                packId = session.pack.id,
                questionIds = session.questions.map { it.id },
                answers = session.answers,
                fromName = session.answererName,
                toName = session.guesserName,
            )
        )
        return ChallengeLinks.webLink(BuildConfig.CHALLENGE_HOST, code)
    }

    // ---------- Links entrantes ----------

    /** Abre un desafío o un resultado recibido por link o por el referrer de instalación. */
    fun openCode(code: String, source: String) {
        val challenge = ChallengeCodec.decode(code)
        val pack = challenge?.let { app.packs.pack(it.packId) }
        val questions = if (challenge != null && pack != null) {
            challenge.questionIds.map { pack.question(it) }
        } else {
            null
        }
        if (challenge == null || pack == null || questions == null || questions.any { it == null }) {
            analytics.log("challenge_opened", "source" to source, "valid" to false)
            _state.update { it.copy(screen = Screen.InvalidChallenge, session = null, reveal = null) }
            return
        }
        val resolved = questions.filterNotNull()
        val guesses = challenge.guesses
        analytics.log(
            "challenge_opened",
            "source" to source,
            "valid" to true,
            "type" to if (guesses != null) "result" else "challenge",
            "pack" to pack.id,
        )
        if (guesses != null) {
            _state.update {
                it.copy(
                    session = Session(
                        mode = Mode.REMOTE,
                        pack = pack,
                        questions = resolved,
                        answererName = challenge.fromName,
                        guesserName = challenge.toName,
                        answers = challenge.answers,
                        guesses = guesses,
                        kind = ResultKind.VIEWER,
                    ),
                    reveal = null,
                    confirmExit = false,
                    screen = Screen.Result,
                )
            }
        } else {
            _state.update {
                it.copy(
                    pendingChallenge = challenge,
                    player1 = challenge.toName.ifBlank { prefs.player1 },
                    reveal = null,
                    confirmExit = false,
                    screen = Screen.ChallengeIntro,
                )
            }
        }
    }

    fun startChallenge() {
        val s = _state.value
        val challenge = s.pendingChallenge ?: return
        val myName = s.player1.trim()
        if (myName.isBlank()) return
        val pack = app.packs.pack(challenge.packId) ?: return
        val questions = challenge.questionIds.mapNotNull { pack.question(it) }
        if (questions.size != challenge.questionIds.size) return
        prefs.player1 = myName
        prefs.player2 = challenge.fromName
        _state.update {
            it.copy(
                player2 = challenge.fromName,
                pendingChallenge = null,
                session = Session(
                    mode = Mode.REMOTE,
                    pack = pack,
                    questions = questions,
                    answererName = challenge.fromName,
                    guesserName = myName,
                    answers = challenge.answers,
                    kind = ResultKind.RECEIVER,
                ),
                screen = Screen.Play(Phase.GUESS),
            )
        }
    }
}
