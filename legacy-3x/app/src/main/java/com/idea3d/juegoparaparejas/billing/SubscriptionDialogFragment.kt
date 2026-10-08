package com.idea3d.juegoparaparejas.billing

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.idea3d.juegoparaparejas.databinding.BottomSheetSubscriptionBinding

class SubscriptionDialogFragment(
    private val onSubscribeClick: () -> Unit,
    private val onDismiss: () -> Unit,
    private val onWatchAdClick: (() -> Unit)? = null
) : BottomSheetDialogFragment() {

    private var binding: BottomSheetSubscriptionBinding? = null
    private var dismissedByUser = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = BottomSheetSubscriptionBinding.inflate(inflater, container, false)
        this.binding = binding

        binding.subscribeButton.setOnClickListener {
            dismissedByUser = true
            onSubscribeClick()
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismissedByUser = true
            dismiss()
        }

        if (onWatchAdClick != null) {
            binding.watchAdButton.setOnClickListener {
                dismissedByUser = true
                onWatchAdClick.invoke()
                dismiss()
            }
        } else {
            binding.watchAdButton.visibility = View.GONE
        }

        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!dismissedByUser) {
            onDismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
