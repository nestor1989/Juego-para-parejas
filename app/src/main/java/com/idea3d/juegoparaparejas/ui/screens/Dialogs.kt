package com.idea3d.juegoparaparejas.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.idea3d.juegoparaparejas.R
import com.idea3d.juegoparaparejas.data.Pack
import com.idea3d.juegoparaparejas.ui.components.PrimaryButton
import com.idea3d.juegoparaparejas.ui.components.SecondaryButton

@Composable
fun PaywallDialog(
    pack: Pack?,
    price: String?,
    onBuy: () -> Unit,
    onWatchAd: (Pack) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("⭐", style = MaterialTheme.typography.headlineMedium) },
        title = { Text(stringResource(R.string.paywall_title), textAlign = TextAlign.Center) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.paywall_body), style = MaterialTheme.typography.bodyLarge)
                PrimaryButton(
                    text = if (price != null) stringResource(R.string.paywall_buy, price)
                    else stringResource(R.string.paywall_buy_noprice),
                    onClick = onBuy,
                )
                if (pack != null) {
                    SecondaryButton(stringResource(R.string.paywall_watch_ad), { onWatchAd(pack) })
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.paywall_later))
            }
        },
    )
}

@Composable
fun AdultGateDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("🔞", style = MaterialTheme.typography.headlineMedium) },
        title = { Text(stringResource(R.string.adult_title)) },
        text = { Text(stringResource(R.string.adult_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.adult_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
fun ExitDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.exit_title)) },
        text = { Text(stringResource(R.string.exit_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.exit_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
