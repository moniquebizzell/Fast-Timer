package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.FastingRepository

class FastNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val repository = FastingRepository.getInstance(context)
        val session = repository.currentSession.value

        // Only trigger if fast is actually active
        if (session.isActive) {
            val protocolLabel = intent.getStringExtra(EXTRA_PROTOCOL_LABEL) ?: session.protocol.label
            NotificationHelper.showFastingCompletedNotification(context, protocolLabel)
        }
    }

    companion object {
        const val EXTRA_PROTOCOL_LABEL = "extra_protocol_label"
        const val EXTRA_TRIGGER_TIME = "extra_trigger_time"
    }
}
