package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.FastingRepository

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val repository = FastingRepository.getInstance(context)
            val session = repository.currentSession.value

            if (session.isActive && session.startTimeMillis > 0L) {
                val now = System.currentTimeMillis()
                if (now >= session.targetEndTimeMillis) {
                    // Fast completed while device was turned off
                    NotificationHelper.showFastingCompletedNotification(context, session.protocol.label)
                } else {
                    // Re-schedule alarm for future completion
                    NotificationHelper.scheduleCompletionNotification(
                        context = context,
                        triggerTimeMillis = session.targetEndTimeMillis,
                        protocolLabel = session.protocol.label
                    )
                }
            }
        }
    }
}
