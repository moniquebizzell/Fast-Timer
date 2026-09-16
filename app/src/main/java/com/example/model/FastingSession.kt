package com.example.model

data class FastingSession(
    val isActive: Boolean = false,
    val startTimeMillis: Long = 0L,
    val protocol: FastingProtocol = FastingProtocol.FAST_16_8
) {
    val targetDurationMillis: Long
        get() = protocol.fastHours * 3600 * 1000L

    val targetEndTimeMillis: Long
        get() = if (startTimeMillis > 0L) startTimeMillis + targetDurationMillis else 0L

    fun elapsedMillis(now: Long = System.currentTimeMillis()): Long {
        if (!isActive || startTimeMillis <= 0L) return 0L
        return (now - startTimeMillis).coerceAtLeast(0L)
    }

    fun remainingMillis(now: Long = System.currentTimeMillis()): Long {
        if (!isActive || startTimeMillis <= 0L) return targetDurationMillis
        val rem = targetEndTimeMillis - now
        return rem.coerceAtLeast(0L)
    }

    fun isCompleted(now: Long = System.currentTimeMillis()): Boolean {
        if (!isActive || startTimeMillis <= 0L) return false
        return now >= targetEndTimeMillis
    }

    fun progressFraction(now: Long = System.currentTimeMillis()): Float {
        if (!isActive || startTimeMillis <= 0L || targetDurationMillis <= 0L) return 0f
        val elapsed = elapsedMillis(now)
        return (elapsed.toFloat() / targetDurationMillis.toFloat()).coerceIn(0f, 1f)
    }

    fun currentFastingStage(now: Long = System.currentTimeMillis()): FastingStage {
        val elapsedHours = elapsedMillis(now) / (3600 * 1000.0)
        return when {
            elapsedHours < 4.0 -> FastingStage.ANABOLIC
            elapsedHours < 12.0 -> FastingStage.GLYCOGEN_DEPLETION
            elapsedHours < 18.0 -> FastingStage.KETOSIS
            else -> FastingStage.AUTOPHAGY
        }
    }
}

enum class FastingStage(
    val stageName: String,
    val timeline: String,
    val description: String
) {
    ANABOLIC(
        stageName = "Blood Sugar Normalizing",
        timeline = "0 – 4 hours",
        description = "Digestion finishes and insulin levels begin dropping to baseline."
    ),
    GLYCOGEN_DEPLETION(
        stageName = "Glycogen Depletion",
        timeline = "4 – 12 hours",
        description = "Body uses stored glycogen in liver and shifts to fat metabolism."
    ),
    KETOSIS(
        stageName = "Fat Burning & Ketosis",
        timeline = "12 – 18 hours",
        description = "Body oxidizes fat for ketone fuel, elevating mental acuity."
    ),
    AUTOPHAGY(
        stageName = "Autophagy & Cell Repair",
        timeline = "18+ hours",
        description = "Cellular recycling cleans out damaged mitochondria and proteins."
    )
}

data class FastHistoryItem(
    val id: String,
    val protocolLabel: String,
    val fastHours: Int,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val completedSuccessfully: Boolean
) {
    val durationMillis: Long
        get() = (endTimeMillis - startTimeMillis).coerceAtLeast(0L)
}
