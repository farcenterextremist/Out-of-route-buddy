/**
 * PROOF OF CONCEPT: MaterialCalendarView Implementation
 * 
 * This is a conceptual example of how we could replace MaterialDatePicker
 * with MaterialCalendarView to achieve:
 * - Locked selection (boundary dates only)
 * - No grayed-out dates
 * - Clickable dates for history viewing
 */

package com.example.outofroutebuddy.presentation.ui.trip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.databinding.DialogCustomCalendarBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.util.Date

/**
 * ✅ CONCEPTUAL: Custom Calendar Dialog using MaterialCalendarView
 * 
 * This would replace the MaterialDatePicker dialog with a custom dialog
 * that gives us full control over date selection and styling.
 */
class CustomCalendarDialog : DialogFragment() {
    
    private var _binding: DialogCustomCalendarBinding? = null
    private val binding get() = _binding!!
    
    private var onPeriodSelected: ((Date, Date) -> Unit)? = null
    private var onHistoryDateClicked: ((Date) -> Unit)? = null
    
    private var periodStartDate: Date? = null
    private var periodEndDate: Date? = null
    private var isLocked: Boolean = true // Selection is locked to boundaries
    
    companion object {
        fun newInstance(
            periodStart: Date,
            periodEnd: Date,
            onPeriodSelected: (Date, Date) -> Unit,
            onHistoryDateClicked: (Date) -> Unit
        ): CustomCalendarDialog {
            return CustomCalendarDialog().apply {
                this.periodStartDate = periodStart
                this.periodEndDate = periodEnd
                this.onPeriodSelected = onPeriodSelected
                this.onHistoryDateClicked = onHistoryDateClicked
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCustomCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCalendar()
        setupButtons()
    }
    
    /**
     * ✅ KEY FEATURE: Setup calendar with locked selection but clickable dates
     */
    private fun setupCalendar() {
        val calendar = binding.calendarView
        
        // 1. Set date range (month boundaries or custom period)
        val startDay = CalendarDay.from(periodStartDate ?: Date())
        val endDay = CalendarDay.from(periodEndDate ?: Date())
        
        calendar.state().edit()
            .setMinimumDate(startDay)
            .setMaximumDate(endDay)
            .commit()
        
        // 2. ✅ HIGHLIGHT BOUNDARY DATES (green for start, red for end)
        // This decorator adds visual highlighting WITHOUT graying out other dates
        calendar.addDecorator(StartDateDecorator(startDay))
        calendar.addDecorator(EndDateDecorator(endDay))
        
        // 3. ✅ PRE-SELECT BOUNDARY RANGE (locked selection)
        calendar.selectedDate = startDay
        calendar.selectRange(startDay, endDay)
        
        // 4. ✅ HANDLE DATE CLICKS
        // This is the KEY: We can distinguish between boundary clicks (locked) 
        // and non-boundary clicks (history viewing)
        calendar.setOnDateChangedListener { widget, date, selected ->
            val clickedDate = date.date
            
            if (isBoundaryDate(clickedDate)) {
                // ✅ LOCKED: User clicked a boundary date
                // Don't allow selection change - keep boundaries selected
                widget.selectedDate = startDay
                widget.selectRange(startDay, endDay)
                
                // Optionally: Show a subtle message that selection is locked
                // Or just do nothing (selection stays on boundaries)
            } else {
                // ✅ HISTORY: User clicked a non-boundary date
                // Open history dialog for that date
                onHistoryDateClicked?.invoke(clickedDate)
                
                // Reset selection back to boundaries after showing history
                widget.selectedDate = startDay
                widget.selectRange(startDay, endDay)
            }
        }
        
        // 5. ✅ DISABLE SELECTION MODE CHANGES (but keep dates clickable)
        // MaterialCalendarView allows us to keep dates clickable while
        // preventing selection changes programmatically
        calendar.selectionMode = MaterialCalendarView.SELECTION_MODE_RANGE
    }
    
    /**
     * ✅ HELPER: Check if a date is a boundary date
     */
    private fun isBoundaryDate(date: Date): Boolean {
        val start = periodStartDate ?: return false
        val end = periodEndDate ?: return false
        
        val startTime = start.time
        val endTime = end.time
        val dateTime = date.time
        
        // Normalize to start of day for comparison
        val startOfDay = normalizeToStartOfDay(startTime)
        val endOfDay = normalizeToStartOfDay(endTime)
        val clickedDay = normalizeToStartOfDay(dateTime)
        
        return clickedDay == startOfDay || clickedDay == endOfDay
    }
    
    private fun normalizeToStartOfDay(timeMillis: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timeMillis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
    
    private fun setupButtons() {
        binding.confirmButton.setOnClickListener {
            // Period selection is always locked to boundaries
            val start = periodStartDate ?: return@setOnClickListener
            val end = periodEndDate ?: return@setOnClickListener
            onPeriodSelected?.invoke(start, end)
            dismiss()
        }
        
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * ✅ DECORATOR: Highlight start date with green circle
 * This decorator styles the start date WITHOUT graying out other dates
 */
class StartDateDecorator(private val startDate: CalendarDay) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == startDate
    }
    
    override fun decorate(view: DayViewFacade) {
        // ✅ Green circle background for start date
        view.setBackgroundDrawable(
            android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(0xFF4CAF50.toInt()) // Green
                setSize(48, 48)
            }
        )
        // ✅ White text for visibility
        view.addSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.WHITE))
    }
}

/**
 * ✅ DECORATOR: Highlight end date with red circle
 * This decorator styles the end date WITHOUT graying out other dates
 */
class EndDateDecorator(private val endDate: CalendarDay) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == endDate
    }
    
    override fun decorate(view: DayViewFacade) {
        // ✅ Red circle background for end date
        view.setBackgroundDrawable(
            android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(0xFFF44336.toInt()) // Red
                setSize(48, 48)
            }
        )
        // ✅ White text for visibility
        view.addSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.WHITE))
    }
}

/**
 * ✅ USAGE EXAMPLE in TripInputFragment:
 * 
 * private fun showCustomCalendarPicker() {
 *     val periodStart = calculatePeriodStart()
 *     val periodEnd = calculatePeriodEnd()
 *     
 *     val dialog = CustomCalendarDialog.newInstance(
 *         periodStart = periodStart,
 *         periodEnd = periodEnd,
 *         onPeriodSelected = { start, end ->
 *             // Period selection (always boundaries)
 *             viewModel.onCalendarPeriodSelected(PeriodMode.CUSTOM, start, end)
 *         },
 *         onHistoryDateClicked = { date ->
 *             // History viewing for any date
 *             showTripHistoryForDate(date)
 *         }
 *     )
 *     dialog.show(parentFragmentManager, "custom_calendar")
 * }
 */
