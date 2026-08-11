package com.idea3d.juegoparaparejas.billing

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class SubscriptionDialogFragment(
    private val onSubscribeClick: () -> Unit,
    private val onDismiss: () -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("🔒 Acceso Premium")
            .setMessage(
                "Desbloquea 6 packs adultos con contenido exclusivo:\n\n" +
                        "• 🎭 Disfraces\n" +
                        "• 🧸 Juguetes\n" +
                        "• 💭 Fantasía\n" +
                        "• 🌸 Fragancias\n" +
                        "• 💕 Romance\n" +
                        "• 🔞 Fetiches\n\n" +
                        "Suscripción mensual - Cancela cuando quieras"
            )
            .setPositiveButton("Suscribirse Ahora") { _, _ ->
                onSubscribeClick()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                onDismiss()
            }
            .setCancelable(false)
            .create()
    }
}

