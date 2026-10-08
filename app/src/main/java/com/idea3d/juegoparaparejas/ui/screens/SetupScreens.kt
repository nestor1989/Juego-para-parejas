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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idea3d.juegoparaparejas.R
import com.idea3d.juegoparaparejas.data.Category
import com.idea3d.juegoparaparejas.data.Pack
import com.idea3d.juegoparaparejas.game.GameEngine
import com.idea3d.juegoparaparejas.game.Mode
import com.idea3d.juegoparaparejas.ui.components.Card
import com.idea3d.juegoparaparejas.ui.components.Chip
import com.idea3d.juegoparaparejas.ui.components.PrimaryButton
import com.idea3d.juegoparaparejas.ui.components.ScreenHeader
import com.idea3d.juegoparaparejas.ui.theme.Gold

@Composable
fun SetupScreen(
    mode: Mode,
    player1: String,
    player2: String,
    onPlayer1: (String) -> Unit,
    onPlayer2: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val remote = mode == Mode.REMOTE
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        ScreenHeader(
            title = stringResource(if (remote) R.string.setup_title_remote else R.string.setup_title_local),
            onBack = onBack,
            backLabel = stringResource(R.string.back),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(if (remote) R.string.setup_remote_explain else R.string.setup_local_explain),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            NameField(
                value = player1,
                onValueChange = onPlayer1,
                label = stringResource(if (remote) R.string.setup_your_name else R.string.setup_player1),
                imeAction = ImeAction.Next,
            )
            NameField(
                value = player2,
                onValueChange = onPlayer2,
                label = stringResource(if (remote) R.string.setup_partner_name else R.string.setup_player2),
                imeAction = ImeAction.Done,
            )
        }
        PrimaryButton(
            text = stringResource(R.string.continue_),
            onClick = onContinue,
            enabled = player1.isNotBlank() && player2.isNotBlank(),
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Composable
fun NameField(value: String, onValueChange: (String) -> Unit, label: String, imeAction: ImeAction) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = imeAction,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun PackPickerScreen(
    packs: List<Pack>,
    isLocked: (Pack) -> Boolean,
    onPick: (Pack) -> Unit,
    onBack: () -> Unit,
) {
    val sections = listOf(
        Category.COUPLE to stringResource(R.string.packs_section_couple),
        Category.FRIENDS to stringResource(R.string.packs_section_friends),
        Category.SPICY to stringResource(R.string.packs_section_spicy),
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        ScreenHeader(stringResource(R.string.packs_title), onBack, stringResource(R.string.back))
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.navigationBarsPadding(),
        ) {
            sections.forEach { (category, title) ->
                val inSection = packs.filter { it.category == category }
                if (inSection.isEmpty()) return@forEach
                item(key = "header-$category") {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp, start = 4.dp),
                    )
                }
                items(inSection.chunked(2), key = { row -> "row-${row.first().id}" }) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { pack ->
                            PackCard(
                                pack = pack,
                                locked = isLocked(pack),
                                onClick = { onPick(pack) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PackCard(pack: Pack, locked: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(150.dp), onClick = onClick) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            Text(pack.emoji, fontSize = 30.sp, modifier = Modifier.weight(1f))
            if (locked) {
                Chip(
                    text = "🔒 " + stringResource(R.string.pack_locked),
                    background = Gold.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Text(
            text = pack.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = pack.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Box(Modifier.weight(1f))
        Text(
            text = stringResource(
                R.string.pack_questions,
                minOf(pack.questions.size, GameEngine.QUESTIONS_PER_ROUND),
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
