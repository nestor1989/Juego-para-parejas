package com.idea3d.juegoparaparejas.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idea3d.juegoparaparejas.R
import com.idea3d.juegoparaparejas.ui.components.Card
import com.idea3d.juegoparaparejas.ui.components.PrimaryButton
import com.idea3d.juegoparaparejas.ui.components.ScreenHeader
import com.idea3d.juegoparaparejas.ui.components.SecondaryButton
import com.idea3d.juegoparaparejas.ui.theme.LocalGradients

@Composable
fun HomeScreen(
    isPremium: Boolean,
    onPlayLocal: () -> Unit,
    onPlayRemote: () -> Unit,
    onHowTo: () -> Unit,
    onPremium: () -> Unit,
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(112.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_subtitle),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(56.dp))

            PrimaryButton(
                text = stringResource(R.string.home_play_local),
                onClick = onPlayLocal,
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.home_play_local_hint),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
            )
            SecondaryButton(
                text = stringResource(R.string.home_play_remote),
                onClick = onPlayRemote,
                contentColor = Color.White,
                borderColor = Color.White,
            )
            Text(
                text = stringResource(R.string.home_play_remote_hint),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
            )
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onHowTo) {
                    Text(stringResource(R.string.home_how_to), color = Color.White)
                }
                if (isPremium) {
                    TextButton(onClick = {}, enabled = false) {
                        Text("✓ " + stringResource(R.string.home_premium_active), color = Color.White)
                    }
                } else {
                    TextButton(onClick = onPremium) {
                        Text("★ " + stringResource(R.string.home_premium), color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun HowToScreen(onBack: () -> Unit, onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        ScreenHeader(stringResource(R.string.howto_title), onBack, stringResource(R.string.back))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HowToStep("1", "🤫", stringResource(R.string.howto_1_title), stringResource(R.string.howto_1_body))
            HowToStep("2", "🤔", stringResource(R.string.howto_2_title), stringResource(R.string.howto_2_body))
            HowToStep("3", "💞", stringResource(R.string.howto_3_title), stringResource(R.string.howto_3_body))
        }
        PrimaryButton(
            text = stringResource(R.string.howto_start),
            onClick = onStart,
            modifier = Modifier.padding(20.dp),
        )
    }
}

@Composable
private fun HowToStep(number: String, emoji: String, title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 32.sp)
            Spacer(Modifier.size(12.dp))
            Column {
                Text("$number. $title", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
