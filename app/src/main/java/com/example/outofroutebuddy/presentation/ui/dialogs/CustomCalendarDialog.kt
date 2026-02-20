package com.example.outofroutebuddy.presentation.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.outofroutebuddy.R
import com.example.outofroutebuddy.databinding.DialogCustomCalendarBinding
import com.example.outofroutebuddy.domain.models.PeriodMode
import com.example.outofroutebuddy.services.PeriodCalculationService
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * ✅ NEW: Custom Calendar Dialog using MaterialCalendarView
 * 
 * This dialog provides:
 * 1. Automatic highlighting of period boundary dates (green for start, red for end)
 * 2. Locked selection - boundary dates are visually locked but all dates remain clickable
 * 3. History viewing - clicking non-boundary dates opens trip history
 * 4. No grayed-out dates - all dates appear normal
 * 
 * @param periodMode STANDARD (first/last of month) or CUSTOM (Thursday before first Friday)
 * @param referenceDate Date used to calculate period boundaries
 * @param onPeriodConfirmed Callback when user confirms period selection (always boundaries)
 * @param onHistoryDateClicked Callback when user clicks a non-boundary date for history
 */
@AndroidEntryPoint
class CustomCalendarDialog : DialogFragment() {
    
    @Inject
    lateinit var periodCalculationService: PeriodCalculationService
    
    private var _binding: DialogCustomCalendarBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Binding accessed before onCreateView or after onDestroyView")
    
    private var periodMode: PeriodMode = PeriodMode.STANDARD
    private var referenceDate: Date? = null
    private var onPeriodConfirmed: ((Date, Date) -> Unit)? = null
    private var onHistoryDateClicked: ((Date) -> Unit)? = null
    
    private var periodStartDate: CalendarDay? = null
    private var periodEndDate: CalendarDay? = null
    private var isSelectionLocked: Boolean = true
    
    companion object {
        private const val ARG_PERIOD_MODE = "period_mode"
        private const val ARG_REFERENCE_DATE = "reference_date"
        
        fun newInstance(
            periodMode: PeriodMode,
            referenceDate: Date,
            onPeriodConfirmed: (Date, Date) -> Unit,
            onHistoryDateClicked: (Date) -> Unit
        ): CustomCalendarDialog {
            return CustomCalendarDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PERIOD_MODE, periodMode)
                    putSerializable(ARG_REFERENCE_DATE, referenceDate)
                }
                this.onPeriodConfirmed = onPeriodConfirmed
                this.onHistoryDateClicked = onHistoryDateClicked
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            periodMode = it.getSerializable(ARG_PERIOD_MODE) as? PeriodMode ?: PeriodMode.STANDARD
            referenceDate = it.getSerializable(ARG_REFERENCE_DATE) as? Date
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
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
        updateTitle()
    }
    
    private fun updateTitle() {
        val title = when (periodMode) {
            PeriodMode.STANDARD -> getString(R.string.statistics_select_day)
            PeriodMode.CUSTOM -> getString(R.string.statistics_select_range)
        }
        binding.calendarTitle.text = title
    }
    
    /**
     * ✅ KEY FEATURE: Setup calendar with automatic highlighting and click handling
     */
    private fun setupCalendar() {
        val calendar = binding.calendarView
        val refDate = referenceDate ?: Date()
        
        // Calculate period boundaries based on mode
        val (startDate, endDate) = calculatePeriodBoundaries(refDate)
        
        // Convert Date to LocalDate for CalendarDay
        val startLocalDate = dateToLocalDate(startDate)
        val endLocalDate = dateToLocalDate(endDate)
        
        periodStartDate = CalendarDay.from(startLocalDate)
        periodEndDate = CalendarDay.from(endLocalDate)
        
        // Set calendar date range
        val minDate = CalendarDay.from(startLocalDate)
        val maxDate = CalendarDay.from(endLocalDate)
        
        calendar.state().edit()
            .setMinimumDate(minDate)
            .setMaximumDate(maxDate)
            .commit()
        
        // ✅ AUTOMATIC HIGHLIGHTING: Add decorators for boundary dates
        calendar.addDecorator(StartDateDecorator(periodStartDate!!))
        calendar.addDecorator(EndDateDecorator(periodEndDate!!))
        
        // ✅ LOCKED SELECTION: Pre-select boundary range
        calendar.selectedDate = periodStartDate
        calendar.selectRange(periodStartDate, periodEndDate)
        
        // ✅ CLICK HANDLING: Distinguish between boundary clicks (locked) and history clicks
        calendar.setOnDateChangedListener(object : OnDateSelectedListener {
            override fun onDateSelected(
                widget: MaterialCalendarView,
                date: CalendarDay,
                selected: Boolean
            ) {
                val clickedLocalDate = date.date
                val clickedDate = localDateToDate(clickedLocalDate)
                
                if (isBoundaryDate(clickedLocalDate)) {
                    // ✅ LOCKED: User clicked a boundary date
                    // Reset selection to boundaries (don't allow change)
                    widget.selectedDate = periodStartDate
                    widget.selectRange(periodStartDate, periodEndDate)
                    // Selection stays locked - no action needed
                } else {
                    // ✅ HISTORY: User clicked a non-boundary date
                    // Open history dialog for that date
                    onHistoryDateClicked?.invoke(clickedDate)
                    
                    // Reset selection back to boundaries after showing history
                    widget.selectedDate = periodStartDate
                    widget.selectRange(periodStartDate, periodEndDate)
                }
            }
        })
        
        // Set selection mode to range
        calendar.selectionMode = MaterialCalendarView.SELECTION_MODE_RANGE
    }
    
    /**
     * ✅ Calculate period boundaries based on mode
     */
    private fun calculatePeriodBoundaries(referenceDate: Date): Pair<Date, Date> {
        return when (periodMode) {
            PeriodMode.STANDARD -> {
                // First and last day of month
                val firstDay = getFirstDayOfMonth(referenceDate)
                val lastDay = getLastDayOfMonth(referenceDate)
                firstDay.time to lastDay.time
            }
            PeriodMode.CUSTOM -> {
                // Thursday before first Friday of month
                val customStart = periodCalculationService.calculateCustomPeriodStart(referenceDate)
                val customEnd = periodCalculationService.calculateCustomPeriodEnd(referenceDate)
                // Normalize to start of day
                customStart.set(Calendar.HOUR_OF_DAY, 0)
                customStart.set(Calendar.MINUTE, 0)
                customStart.set(Calendar.SECOND, 0)
                customStart.set(Calendar.MILLISECOND, 0)
                customEnd.set(Calendar.HOUR_OF_DAY, 0)
                customEnd.set(Calendar.MINUTE, 0)
                customEnd.set(Calendar.SECOND, 0)
                customEnd.set(Calendar.MILLISECOND, 0)
                customStart.time to customEnd.time
            }
        }
    }
    
    private fun getFirstDayOfMonth(date: Date): Calendar {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }
    
    private fun getLastDayOfMonth(date: Date): Calendar {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }
    
    /**
     * ✅ Check if a date is a boundary date (start or end)
     */
    private fun isBoundaryDate(date: LocalDate): Boolean {
        val start = periodStartDate?.date ?: return false
        val end = periodEndDate?.date ?: return false
        
        return date == start || date == end
    }
    
    /**
     * Convert Date to LocalDate using Calendar (compatible with ThreeTen Backport)
     */
    private fun dateToLocalDate(date: Date): LocalDate {
        val cal = Calendar.getInstance()
        cal.time = date
        return LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }
    
    /**
     * Convert LocalDate to Date using Calendar (compatible with ThreeTen Backport)
     */
    private fun localDateToDate(localDate: LocalDate): Date {
        val cal = Calendar.getInstance()
        cal.set(localDate.year, localDate.monthValue - 1, localDate.dayOfMonth, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }
    
    private fun normalizeToStartOfDay(timeMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
    
    private fun setupButtons() {
        binding.confirmButton.setOnClickListener {
            // Period selection is always locked to boundaries
            val startLocalDate = periodStartDate?.date ?: return@setOnClickListener
            val endLocalDate = periodEndDate?.date ?: return@setOnClickListener
            val start = localDateToDate(startLocalDate)
            val end = localDateToDate(endLocalDate)
            onPeriodConfirmed?.invoke(start, end)
            dismiss()
        }
        
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9f).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * ✅ DECORATOR: Highlight start date with green circle
 * This automatically highlights the period start date without graying out other dates
 */
class StartDateDecorator(private val startDate: CalendarDay) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == startDate
    }
    
    override fun decorate(view: DayViewFacade) {
        // Green circle background for start date
        view.setBackgroundDrawable(
            android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(0xFF4CAF50.toInt()) // Green
                setSize(48, 48)
            }
        )
        // White text for visibility
        view.addSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.WHITE))
    }
}

/**
 * ✅ DECORATOR: Highlight end date with red circle
 * This automatically highlights the period end date without graying out other dates
 */
class EndDateDecorator(private val endDate: CalendarDay) : DayViewDecorator {
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == endDate
    }
    
    override fun decorate(view: DayViewFacade) {
        // Red circle background for end date
        view.setBackgroundDrawable(
            android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(0xFFF44336.toInt()) // Red
                setSize(48, 48)
            }
        )
        // White text for visibility
        view.addSpan(android.text.style.ForegroundColorSpan(android.graphics.Color.WHITE))
    }
}
