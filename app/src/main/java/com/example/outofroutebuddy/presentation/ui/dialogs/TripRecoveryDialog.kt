package com.example.outofroutebuddy.presentation.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.data.TripPersistenceManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ NEW: Trip Recovery Dialog for Phone Restart & App Closing Recovery
 *
 * This dialog appears when the app detects a previously active trip that can be recovered.
 * Users can choose to:
 * - Continue the previous trip
 * - Start a new trip
 */
class TripRecoveryDialog : DialogFragment() {
    
    companion object {
        private const val ARG_SAVED_STATE = "saved_state"

        fun newInstance(savedState: TripPersistenceManager.SavedTripState): TripRecoveryDialog {
            val dialog = TripRecoveryDialog()
            val args = Bundle()
            args.putSerializable(ARG_SAVED_STATE, savedState)
            dialog.arguments = args
            return dialog
        }
    }
    
    interface TripRecoveryListener {
        fun onContinueTrip(savedState: TripPersistenceManager.SavedTripState)
        fun onStartNewTrip()
    }
    
    private var listener: TripRecoveryListener? = null
    private lateinit var savedState: TripPersistenceManager.SavedTripState
    
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? TripRecoveryListener
            ?: context as? TripRecoveryListener
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            savedState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                args.getSerializable(ARG_SAVED_STATE, TripPersistenceManager.SavedTripState::class.java)!!
            } else {
                @Suppress("DEPRECATION")
                args.getSerializable(ARG_SAVED_STATE) as TripPersistenceManager.SavedTripState
            }
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_trip_recovery)
        
        // ✅ FIX: Prevent dialog from being dismissed by clicking outside or back button
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        
        setupViews(dialog)
        
        return dialog
    }
    
    private fun setupViews(dialog: Dialog) {
        val titleText = dialog.findViewById<TextView>(R.id.text_recovery_title)
        val tripInfoText = dialog.findViewById<TextView>(R.id.text_trip_info)
        val continueButton = dialog.findViewById<Button>(R.id.button_continue_trip)
        val newTripButton = dialog.findViewById<Button>(R.id.button_start_new)
        
        // Set title
        titleText.text = "Trip Recovery"
        
        // Format simplified trip information
        val tripInfo = buildString {
            appendLine("Loaded: ${String.format("%.1f", savedState.loadedMiles)} mi")
            appendLine("Bounce: ${String.format("%.1f", savedState.bounceMiles)} mi")
            appendLine("Actual: ${String.format("%.1f", savedState.actualMiles)} mi")
        }
        
        tripInfoText.text = tripInfo
        
        // Set up button listeners
        continueButton.setOnClickListener {
            listener?.onContinueTrip(savedState)
            dismiss()
        }
        
        newTripButton.setOnClickListener {
            listener?.onStartNewTrip()
            dismiss()
        }
    }
    
    override fun onDetach() {
        super.onDetach()
        listener = null
    }
    
    // ✅ FIX: Prevent back button from dismissing dialog and clean up window edges
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        
        // Clean up window styling
        dialog?.window?.decorView?.setPadding(0, 0, 0, 0)
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
