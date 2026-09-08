package com.idea3d.juegoparaparejas.billing

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        isCancelable = false
        val binding = BottomSheetSubscriptionBinding.inflate(inflater, container, false)
        this.binding = binding

        binding.subscribeButton.setOnClickListener { onSubscribeClick() }

        binding.cancelButton.setOnClickListener {
            dismiss()
            onDismiss()
        }

        if (onWatchAdClick != null) {
            binding.watchAdButton.setOnClickListener { onWatchAdClick.invoke() }
        } else {
            binding.watchAdButton.visibility = View.GONE
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
