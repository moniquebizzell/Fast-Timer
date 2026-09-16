package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.FastHistoryItem
import com.example.model.FastingProtocol
import com.example.model.FastingSession
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class FastingRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentSession = MutableStateFlow(loadSession())
    val currentSession: StateFlow<FastingSession> = _currentSession.asStateFlow()

    private val _history = MutableStateFlow(loadHistory())
    val history: StateFlow<List<FastHistoryItem>> = _history.asStateFlow()

    private fun loadSession(): FastingSession {
        val isActive = prefs.getBoolean(KEY_IS_ACTIVE, false)
        val startTime = prefs.getLong(KEY_START_TIME, 0L)
        val protocolLabel = prefs.getString(KEY_PROTOCOL, FastingProtocol.FAST_16_8.label)
            ?: FastingProtocol.FAST_16_8.label

        return FastingSession(
            isActive = isActive,
            startTimeMillis = startTime,
            protocol = FastingProtocol.fromLabel(protocolLabel)
        )
    }

    fun startFast(protocol: FastingProtocol, customStartTimeMillis: Long? = null) {
        val startTime = customStartTimeMillis ?: System.currentTimeMillis()
        val session = FastingSession(
            isActive = true,
            startTimeMillis = startTime,
            protocol = protocol
        )

        prefs.edit()
            .putBoolean(KEY_IS_ACTIVE, true)
            .putLong(KEY_START_TIME, startTime)
            .putString(KEY_PROTOCOL, protocol.label)
            .apply()

        _currentSession.value = session

        // Schedule local notification for when the fasting window finishes
        NotificationHelper.scheduleCompletionNotification(
            context = context,
            triggerTimeMillis = session.targetEndTimeMillis,
            protocolLabel = protocol.label
        )
    }

    fun updateStartTime(newStartTimeMillis: Long) {
        val current = _currentSession.value
        if (!current.isActive) return

        val updated = current.copy(startTimeMillis = newStartTimeMillis)
        prefs.edit()
            .putLong(KEY_START_TIME, newStartTimeMillis)
            .apply()

        _currentSession.value = updated

        // Re-schedule notification with updated target end time
        NotificationHelper.scheduleCompletionNotification(
            context = context,
            triggerTimeMillis = updated.targetEndTimeMillis,
            protocolLabel = updated.protocol.label
        )
    }

    fun endFast(cancelledEarly: Boolean = false) {
        val current = _currentSession.value
        val endTime = System.currentTimeMillis()

        if (current.isActive && current.startTimeMillis > 0L) {
            val record = FastHistoryItem(
                id = UUID.randomUUID().toString(),
                protocolLabel = current.protocol.label,
                fastHours = current.protocol.fastHours,
                startTimeMillis = current.startTimeMillis,
                endTimeMillis = endTime,
                completedSuccessfully = !cancelledEarly && (endTime >= current.targetEndTimeMillis)
            )
            addHistoryRecord(record)
        }

        prefs.edit()
            .putBoolean(KEY_IS_ACTIVE, false)
            .putLong(KEY_START_TIME, 0L)
            .apply()

        _currentSession.value = FastingSession(
            isActive = false,
            startTimeMillis = 0L,
            protocol = current.protocol
        )

        // Cancel scheduled notification
        NotificationHelper.cancelScheduledNotification(context)
    }

    private fun loadHistory(): List<FastHistoryItem> {
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<FastHistoryItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    FastHistoryItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        protocolLabel = obj.optString("protocol", "16:8"),
                        fastHours = obj.optInt("fastHours", 16),
                        startTimeMillis = obj.optLong("startTime", 0L),
                        endTimeMillis = obj.optLong("endTime", 0L),
                        completedSuccessfully = obj.optBoolean("completed", false)
                    )
                )
            }
        } catch (_: Exception) {
            // Ignore corrupted JSON
        }
        return list.sortedByDescending { it.endTimeMillis }
    }

    private fun addHistoryRecord(record: FastHistoryItem) {
        val updatedList = (_history.value.toMutableList().apply { add(0, record) }).take(30)
        _history.value = updatedList

        try {
            val array = JSONArray()
            for (item in updatedList) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("protocol", item.protocolLabel)
                    put("fastHours", item.fastHours)
                    put("startTime", item.startTimeMillis)
                    put("endTime", item.endTimeMillis)
                    put("completed", item.completedSuccessfully)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
        } catch (_: Exception) {
            // Ignore JSON serialization errors
        }
    }

    fun isFastActive(): Boolean = prefs.getBoolean(KEY_IS_ACTIVE, false)

    companion object {
        private const val PREFS_NAME = "fast_timer_preferences"
        const val KEY_IS_ACTIVE = "key_is_fast_active"
        const val KEY_START_TIME = "key_fast_start_time"
        const val KEY_PROTOCOL = "key_fast_protocol"
        const val KEY_HISTORY = "key_fast_history"

        @Volatile
        private var instance: FastingRepository? = null

        fun getInstance(context: Context): FastingRepository {
            return instance ?: synchronized(this) {
                instance ?: FastingRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
