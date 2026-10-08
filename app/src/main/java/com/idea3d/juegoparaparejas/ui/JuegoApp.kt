package com.idea3d.juegoparaparejas.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.idea3d.juegoparaparejas.App
import com.idea3d.juegoparaparejas.R
import com.idea3d.juegoparaparejas.game.ChallengeLinks
import com.idea3d.juegoparaparejas.game.Mode
import com.idea3d.juegoparaparejas.monetization.BillingManager
import com.idea3d.juegoparaparejas.ui.screens.AdultGateDialog
import com.idea3d.juegoparaparejas.ui.screens.ChallengeIntroScreen
import com.idea3d.juegoparaparejas.ui.screens.ChallengeReadyScreen
import com.idea3d.juegoparaparejas.ui.screens.ExitDialog
import com.idea3d.juegoparaparejas.ui.screens.HandoffScreen
import com.idea3d.juegoparaparejas.ui.screens.HomeScreen
import com.idea3d.juegoparaparejas.ui.screens.HowToScreen
import com.idea3d.juegoparaparejas.ui.screens.InvalidChallengeScreen
import com.idea3d.juegoparaparejas.ui.screens.PackPickerScreen
import com.idea3d.juegoparaparejas.ui.screens.PaywallDialog
import com.idea3d.juegoparaparejas.ui.screens.PlayScreen
import com.idea3d.juegoparaparejas.ui.screens.ResultActions
import com.idea3d.juegoparaparejas.ui.screens.ResultScreen
import com.idea3d.juegoparaparejas.ui.screens.SetupScreen
import com.idea3d.juegoparaparejas.util.Review
import com.idea3d.juegoparaparejas.util.Share
import com.idea3d.juegoparaparejas.util.findActivity
import kotlinx.coroutines.delay

@Composable
fun JuegoApp(vm: GameViewModel) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val app = context.applicationContext as App

    val state by vm.state.collectAsStateWithLifecycle()
    val isPremium by app.billing.isPremium.collectAsStateWithLifecycle()
    val price by app.billing.price.collectAsStateWithLifecycle()
    val purchaseEvent by app.billing.purchaseEvents.collectAsStateWithLifecycle()

    fun toast(resId: Int) = Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()

    /** Interstitial (si corresponde por las reglas de AdsManager) y después la acción. */
    fun afterInterstitial(action: () -> Unit) {
        if (activity == null) action() else app.ads.maybeShowInterstitial(activity, action)
    }

    BackHandler(enabled = state.screen != Screen.Home) { vm.back() }

    LaunchedEffect(purchaseEvent) {
        when (purchaseEvent) {
            BillingManager.PurchaseEvent.Success -> {
                toast(R.string.purchase_thanks)
                vm.onPurchaseSuccess()
                app.billing.consumeEvent()
            }
            BillingManager.PurchaseEvent.Failed -> {
                toast(R.string.purchase_error)
                app.billing.consumeEvent()
            }
            null -> Unit
        }
    }

    LaunchedEffect(isPremium) {
        if (!isPremium) app.ads.preload()
    }

    LaunchedEffect(state.askReview) {
        if (state.askReview) {
            delay(1500)
            activity?.let { Review.request(it) }
            vm.consumeReviewRequest()
        }
    }

    when (val screen = state.screen) {
        Screen.Home -> HomeScreen(
            isPremium = isPremium,
            onPlayLocal = { vm.openSetup(Mode.LOCAL) },
            onPlayRemote = { vm.openSetup(Mode.REMOTE) },
            onHowTo = vm::openHowTo,
            onPremium = { vm.openPaywall(null) },
        )

        Screen.HowTo -> HowToScreen(onBack = vm::goHome, onStart = { vm.openSetup(Mode.LOCAL) })

        is Screen.Setup -> SetupScreen(
            mode = screen.mode,
            player1 = state.player1,
            player2 = state.player2,
            onPlayer1 = vm::setPlayer1,
            onPlayer2 = vm::setPlayer2,
            onBack = vm::goHome,
            onContinue = { vm.confirmSetup(screen.mode) },
        )

        is Screen.Packs -> PackPickerScreen(
            packs = vm.packs,
            isLocked = { pack -> vm.isLocked(pack, isPremium) },
            onPick = { pack -> vm.selectPack(pack, screen.mode) },
            onBack = { vm.back() },
        )

        is Screen.Handoff -> state.session?.let { session ->
            HandoffScreen(
                session = session,
                phase = screen.phase,
                onReady = { vm.handoffReady(screen.phase) },
                onExit = { vm.back() },
            )
        }

        is Screen.Play -> state.session?.let { session ->
            PlayScreen(
                session = session,
                phase = screen.phase,
                reveal = state.reveal,
                onChoice = { choice ->
                    if (screen.phase == com.idea3d.juegoparaparejas.game.Phase.ANSWER) vm.answer(choice) else vm.guess(choice)
                },
                onExit = { vm.back() },
            )
        }

        Screen.Result -> state.session?.let { session ->
            ResultScreen(
                session = session,
                actions = ResultActions(
                    onSwitchRoles = { afterInterstitial(vm::switchRoles) },
                    onOtherPack = { afterInterstitial(vm::otherPack) },
                    onHome = { afterInterstitial(vm::goHome) },
                    onChallengeBack = { afterInterstitial(vm::challengeBack) },
                    onRematch = vm::otherPack,
                    onShare = {
                        val text = when (session.kind) {
                            ResultKind.RECEIVER -> context.getString(
                                R.string.share_remote_result,
                                session.score,
                                session.total,
                                vm.resultLink() ?: ChallengeLinks.PLAY_URL,
                            )
                            else -> context.getString(
                                R.string.share_local_result,
                                session.guesserName,
                                session.answererName,
                                session.percent,
                                ChallengeLinks.PLAY_URL,
                            )
                        }
                        vm.onResultShared()
                        if (session.kind == ResultKind.RECEIVER) {
                            Share.whatsApp(context, text, context.getString(R.string.share_chooser))
                        } else {
                            Share.chooser(context, text, context.getString(R.string.share_chooser))
                        }
                    },
                ),
            )
        }

        Screen.ChallengeReady -> {
            val session = state.session
            val link = state.challengeLink
            if (session != null && link != null) {
                val message = context.getString(
                    R.string.share_challenge_text,
                    session.answererName,
                    session.total,
                    link,
                )
                ChallengeReadyScreen(
                    partnerName = session.guesserName,
                    onWhatsApp = {
                        vm.onChallengeShared("whatsapp")
                        Share.whatsApp(context, message, context.getString(R.string.share_chooser))
                    },
                    onOtherApp = {
                        vm.onChallengeShared("chooser")
                        Share.chooser(context, message, context.getString(R.string.share_chooser))
                    },
                    onCopy = {
                        vm.onChallengeShared("copy")
                        Share.copy(context, link)
                        toast(R.string.link_copied)
                    },
                    onHome = vm::goHome,
                )
            }
        }

        Screen.ChallengeIntro -> state.pendingChallenge?.let { challenge ->
            ChallengeIntroScreen(
                challenge = challenge,
                pack = app.packs.pack(challenge.packId),
                myName = state.player1,
                onMyName = vm::setPlayer1,
                onStart = vm::startChallenge,
                onHome = vm::goHome,
            )
        }

        Screen.InvalidChallenge -> InvalidChallengeScreen(
            onUpdate = { Share.openUrl(context, ChallengeLinks.PLAY_URL) },
            onHome = vm::goHome,
        )
    }

    if (state.confirmExit) {
        ExitDialog(onConfirm = vm::confirmExit, onDismiss = vm::cancelExit)
    }

    state.adultGatePack?.let {
        AdultGateDialog(onConfirm = vm::confirmAdult, onDismiss = vm::dismissAdultGate)
    }

    if (state.showPaywall && !isPremium) {
        PaywallDialog(
            pack = state.paywallPack,
            price = price,
            onBuy = { activity?.let { app.billing.launchPurchase(it) } },
            onWatchAd = { pack ->
                activity?.let { act ->
                    app.ads.showRewarded(
                        activity = act,
                        onReward = { vm.unlockWithAd(pack) },
                        onUnavailable = { toast(R.string.ad_not_ready) },
                    )
                }
            },
            onDismiss = vm::dismissPaywall,
        )
    }
}
