package com.idea3d.juegoparaparejas.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idea3d.juegoparaparejas.R
import com.idea3d.juegoparaparejas.game.Phase
import com.idea3d.juegoparaparejas.ui.Reveal
import com.idea3d.juegoparaparejas.ui.Session
import com.idea3d.juegoparaparejas.ui.components.AnswerOption
import com.idea3d.juegoparaparejas.ui.components.Chip
import com.idea3d.juegoparaparejas.ui.components.OptionState
import com.idea3d.juegoparaparejas.ui.components.PrimaryButton
import com.idea3d.juegoparaparejas.ui.theme.LocalGradients

/** Pantalla de "pasale el teléfono": oculta todo hasta que la persona correcta lo tiene. */
@Composable
fun HandoffScreen(session: Session, phase: Phase, onReady: () -> Unit, onExit: () -> Unit) {
    val holder = if (phase == Phase.ANSWER) session.answererName else session.guesserName
    val body = if (phase == Phase.ANSWER) {
        stringResource(R.string.handoff_answer, session.answererName, session.guesserName)
    } else {
        stringResource(R.string.handoff_guess, session.guesserName, session.answererName)
    }
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
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onExit) { Text("✕", color = Color.White, fontSize = 20.sp) }
            }
            Spacer(Modifier.height(48.dp))
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("📱", fontSize = 60.sp)
            }
            Spacer(Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.handoff_title, holder),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = body,
                fontSize = 17.sp,
                color = Color.White.copy(alpha = 0.92f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(48.dp))
            PrimaryButton(
                text = stringResource(R.string.handoff_ready, holder),
                onClick = onReady,
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun PlayScreen(
    session: Session,
    phase: Phase,
    reveal: Reveal?,
    onChoice: (Int) -> Unit,
    onExit: () -> Unit,
) {
    val index = if (phase == Phase.ANSWER) session.answers.size else session.guesses.size
    val question = session.questions.getOrNull(index) ?: return
    val hint = if (phase == Phase.ANSWER) {
        stringResource(R.string.play_answer_hint, session.answererName)
    } else {
        stringResource(R.string.play_guess_hint, session.guesserName, session.answererName)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onExit) { Text("✕", fontSize = 20.sp) }
            Text(
                text = stringResource(R.string.play_progress, index + 1, session.total),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Chip(
                text = "${session.pack.emoji} ${session.pack.name}",
                background = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(end = 12.dp),
            )
        }
        LinearProgressIndicator(
            progress = { (index.toFloat() / session.total).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
        )

        AnimatedContent(
            targetState = index,
            // "Fade through": la pregunta anterior sale antes de que entre la nueva, sin superponerse.
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = 140, delayMillis = 70)) togetherWith
                    fadeOut(animationSpec = tween(durationMillis = 70))
            },
            label = "question",
            modifier = Modifier.weight(1f),
        ) { shownIndex ->
            val shown = session.questions.getOrNull(shownIndex) ?: question
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = shown.text,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                shown.answers.forEachIndexed { i, answerText ->
                    val state = when {
                        reveal == null || phase != Phase.GUESS -> OptionState.IDLE
                        i == reveal.correct -> OptionState.CORRECT
                        i == reveal.chosen -> OptionState.WRONG
                        else -> OptionState.DIMMED
                    }
                    AnswerOption(text = answerText, state = state, onClick = { onChoice(i) })
                }
            }
        }
    }
}
