package com.idea3d.juegoparaparejas.billing

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.idea3d.juegoparaparejas.R

class SubscriptionDialogFragment(
    private val onSubscribeClick: () -> Unit,
    private val onDismiss: () -> Unit,
    private val onWatchAdClick: (() -> Unit)? = null
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.sub_dialog_title))
            .setMessage(getString(R.string.sub_dialog_message))
            .setPositiveButton(getString(R.string.sub_dialog_subscribe)) { _, _ ->
                onSubscribeClick()
            }
            .setNegativeButton(getString(R.string.sub_dialog_cancel)) { dialog, _ ->
                dialog.dismiss()
                onDismiss()
            }
            .setCancelable(false)

        if (onWatchAdClick != null) {
            builder.setNeutralButton(getString(R.string.sub_dialog_watch_ad)) { _, _ ->
                onWatchAdClick.invoke()
            }
        }

        return builder.create()
    }
}

