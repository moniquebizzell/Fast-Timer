package com.example.ui

import android.app.Application
import android.text.format.DateFormat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FastingRepository
import com.example.model.FastHistoryItem
import com.example.model.FastingProtocol
import com.example.model.FastingSession
import com.example.model.FastingStage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FastingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FastingRepository.getInstance(application)

    val currentSession: StateFlow<FastingSession> = repository.currentSession
    val history: StateFlow<List<FastHistoryItem>> = repository.history

    // Live 1-second clock ticker for smooth timer countdown
    val nowMillis: StateFlow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000L)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = System.currentTimeMillis()
    )

    private val _selectedProtocol = MutableStateFlow(FastingProtocol.FAST_16_8)
    val selectedProtocol: StateFlow<FastingProtocol> = _selectedProtocol.asStateFlow()

    private val _showCancelDialog = MutableStateFlow(false)
    val showCancelDialog: StateFlow<Boolean> = _showCancelDialog.asStateFlow()

    private val _showAdjustDialog = MutableStateFlow(false)
    val showAdjustDialog: StateFlow<Boolean> = _showAdjustDialog.asStateFlow()

    fun selectProtocol(protocol: FastingProtocol) {
        _selectedProtocol.value = protocol
    }

    fun startFast(protocol: FastingProtocol = _selectedProtocol.value, customStartTime: Long? = null) {
        repository.startFast(protocol, customStartTime)
    }

    fun endFastEarly() {
        _showCancelDialog.value = false
        repository.endFast(cancelledEarly = true)
    }

    fun completeFast() {
        repository.endFast(cancelledEarly = false)
    }

    fun adjustStartTime(newStartTimeMillis: Long) {
        _showAdjustDialog.value = false
        repository.updateStartTime(newStartTimeMillis)
    }

    fun openCancelDialog() {
        _showCancelDialog.value = true
    }

    fun dismissCancelDialog() {
        _showCancelDialog.value = false
    }

    fun openAdjustDialog() {
        _showAdjustDialog.value = true
    }

    fun dismissAdjustDialog() {
        _showAdjustDialog.value = false
    }

    // Helper formatting methods for consistent UI presentation
    fun formatTargetTime(targetMillis: Long): String {
        if (targetMillis <= 0L) return "--:--"
        val context = getApplication<Application>()
        val is24Hour = DateFormat.is24HourFormat(context)
        val timePattern = if (is24Hour) "HH:mm" else "h:mm a"
        val timeFormatter = SimpleDateFormat(timePattern, Locale.getDefault())
        val formattedTime = timeFormatter.format(Date(targetMillis))

        val nowCal = Calendar.getInstance()
        val targetCal = Calendar.getInstance().apply { timeInMillis = targetMillis }

        val dayDiff = targetCal.get(Calendar.DAY_OF_YEAR) - nowCal.get(Calendar.DAY_OF_YEAR)
        val yearDiff = targetCal.get(Calendar.YEAR) - nowCal.get(Calendar.YEAR)

        return when {
            yearDiff == 0 && dayDiff == 0 -> "Today at $formattedTime"
            yearDiff == 0 && dayDiff == 1 -> "Tomorrow at $formattedTime"
            else -> {
                val dayFormatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                "${dayFormatter.format(Date(targetMillis))} at $formattedTime"
            }
        }
    }

    fun formatTargetEatTimeOnly(targetMillis: Long): String {
        if (targetMillis <= 0L) return "--:--"
        val context = getApplication<Application>()
        val is24Hour = DateFormat.is24HourFormat(context)
        val timePattern = if (is24Hour) "HH:mm" else "h:mm a"
        val timeFormatter = SimpleDateFormat(timePattern, Locale.getDefault())
        return timeFormatter.format(Date(targetMillis))
    }

    fun formatTimeOnly(timeMillis: Long): String {
        if (timeMillis <= 0L) return "--:--"
        val context = getApplication<Application>()
        val is24Hour = DateFormat.is24HourFormat(context)
        val timePattern = if (is24Hour) "HH:mm" else "h:mm a"
        val timeFormatter = SimpleDateFormat(timePattern, Locale.getDefault())
        return timeFormatter.format(Date(timeMillis))
    }

    fun formatCountdown(remainingMillis: Long): String {
        val totalSeconds = (remainingMillis / 1000).coerceAtLeast(0)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun formatDurationShort(durationMillis: Long): String {
        val totalMinutes = (durationMillis / (1000 * 60)).coerceAtLeast(0)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}
