package com.idea3d.juegoparaparejas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idea3d.juegoparaparejas.R
import com.idea3d.juegoparaparejas.data.Pack
import com.idea3d.juegoparaparejas.game.Challenge
import com.idea3d.juegoparaparejas.game.GameEngine
import com.idea3d.juegoparaparejas.ui.ResultKind
import com.idea3d.juegoparaparejas.ui.Session
import com.idea3d.juegoparaparejas.ui.components.Card
import com.idea3d.juegoparaparejas.ui.components.PrimaryButton
import com.idea3d.juegoparaparejas.ui.components.ScoreRing
import com.idea3d.juegoparaparejas.ui.components.ScreenHeader
import com.idea3d.juegoparaparejas.ui.components.SecondaryButton
import com.idea3d.juegoparaparejas.ui.theme.Correct
import com.idea3d.juegoparaparejas.ui.theme.LocalGradients
import com.idea3d.juegoparaparejas.ui.theme.Wrong

data class ResultActions(
    val onSwitchRoles: () -> Unit,
    val onOtherPack: () -> Unit,
    val onShare: () -> Unit,
    val onHome: () -> Unit,
    val onChallengeBack: () -> Unit,
    val onRematch: () -> Unit,
)

@Composable
fun ResultScreen(session: Session, actions: ResultActions) {
    val tierText = when (GameEngine.tier(session.percent)) {
        3 -> stringResource(R.string.result_tier_3)
        2 -> stringResource(R.string.result_tier_2)
        1 -> stringResource(R.string.result_tier_1)
        else -> stringResource(R.string.result_tier_0)
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (session.kind == ResultKind.VIEWER) {
                    Text(
                        text = stringResource(R.string.result_viewer_title, session.guesserName),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    text = stringResource(R.string.result_knows, session.guesserName, session.answererName),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                ScoreRing(percent = session.percent)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = tierText,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.result_score, session.score, session.total),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
        }

        item(key = "actions") {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                when (session.kind) {
                    ResultKind.LOCAL -> {
                        PrimaryButton(stringResource(R.string.result_switch), actions.onSwitchRoles)
                        SecondaryButton(stringResource(R.string.result_share), actions.onShare)
                        SecondaryButton(stringResource(R.string.result_other_pack), actions.onOtherPack)
                    }
                    ResultKind.RECEIVER -> {
                        PrimaryButton(stringResource(R.string.result_send_back, session.answererName), actions.onShare)
                        SecondaryButton(stringResource(R.string.result_your_turn, session.answererName), actions.onChallengeBack)
                        SecondaryButton(stringResource(R.string.result_other_pack), actions.onOtherPack)
                    }
                    ResultKind.VIEWER -> {
                        PrimaryButton(stringResource(R.string.result_rematch), actions.onRematch)
                    }
                }
                TextButton(onClick = actions.onHome, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.result_home))
                }
            }
        }

        item(key = "review-title") {
            Text(
                text = stringResource(R.string.result_review),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )
        }

        itemsIndexed(session.questions, key = { _, q -> "q-${q.id}" }) { i, question ->
            val answer = session.answers.getOrNull(i)
            val guess = session.guesses.getOrNull(i)
            val match = answer != null && answer == guess
            Card(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(if (match) Correct else Wrong, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(if (match) "✓" else "✕", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(question.text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        if (answer != null) {
                            LabeledAnswer(
                                label = stringResource(R.string.result_answered, session.answererName),
                                value = question.answers.getOrElse(answer) { "" },
                            )
                        }
                        if (guess != null && !match) {
                            LabeledAnswer(
                                label = stringResource(R.string.result_guessed, session.guesserName),
                                value = question.answers.getOrElse(guess) { "" },
                            )
                        }
                    }
                }
            }
        }

        item(key = "bottom") { Spacer(Modifier.navigationBarsPadding()) }
    }
}

@Composable
private fun LabeledAnswer(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun ChallengeReadyScreen(
    partnerName: String,
    onWhatsApp: () -> Unit,
    onOtherApp: () -> Unit,
    onCopy: () -> Unit,
    onHome: () -> Unit,
) {
    GradientMessage(
        emoji = "💌",
        title = stringResource(R.string.challenge_ready_title),
        body = stringResource(R.string.challenge_ready_body, partnerName),
    ) {
        PrimaryButton(
            text = stringResource(R.string.send_whatsapp),
            onClick = onWhatsApp,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary,
        )
        SecondaryButton(stringResource(R.string.send_other), onOtherApp, contentColor = Color.White, borderColor = Color.White)
        TextButton(onClick = onCopy, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.copy_link), color = Color.White)
        }
        TextButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.result_home), color = Color.White.copy(alpha = 0.85f))
        }
    }
}

@Composable
fun ChallengeIntroScreen(
    challenge: Challenge,
    pack: Pack?,
    myName: String,
    onMyName: (String) -> Unit,
    onStart: () -> Unit,
    onHome: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        ScreenHeader("", onHome, stringResource(R.string.result_home))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(pack?.emoji ?: "💌", fontSize = 64.sp)
            Text(
                text = stringResource(R.string.intro_title, challenge.fromName),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(
                    R.string.intro_body,
                    challenge.fromName,
                    challenge.questionIds.size,
                    pack?.name.orEmpty(),
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            NameField(
                value = myName,
                onValueChange = onMyName,
                label = stringResource(R.string.setup_your_name),
                imeAction = androidx.compose.ui.text.input.ImeAction.Done,
            )
        }
        PrimaryButton(
            text = stringResource(R.string.intro_start),
            onClick = onStart,
            enabled = myName.isNotBlank(),
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Composable
fun InvalidChallengeScreen(onUpdate: () -> Unit, onHome: () -> Unit) {
    GradientMessage(
        emoji = "🤔",
        title = stringResource(R.string.invalid_title),
        body = stringResource(R.string.invalid_body),
    ) {
        PrimaryButton(
            text = stringResource(R.string.invalid_update),
            onClick = onUpdate,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary,
        )
        TextButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.result_home), color = Color.White)
        }
    }
}

@Composable
private fun GradientMessage(
    emoji: String,
    title: String,
    body: String,
    buttons: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalGradients.current.hero),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(56.dp))
            Text(emoji, fontSize = 72.sp)
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Text(
                text = body,
                fontSize = 17.sp,
                color = Color.White.copy(alpha = 0.92f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            buttons()
        }
    }
}
