package com.example.model

enum class FastingProtocol(
    val label: String,
    val fastHours: Int,
    val eatHours: Int,
    val title: String,
    val subtitle: String,
    val description: String
) {
    FAST_16_8(
        label = "16:8",
        fastHours = 16,
        eatHours = 8,
        title = "16:8 LeanGains",
        subtitle = "Most Popular",
        description = "16 hours fasting with an 8-hour eating window. Ideal for daily balance, sustained energy, and metabolic health."
    ),
    FAST_18_6(
        label = "18:6",
        fastHours = 18,
        eatHours = 6,
        title = "18:6 Accelerated",
        subtitle = "Intermediate",
        description = "18 hours fasting with a 6-hour eating window. Accelerates fat burning and deepens glycogen depletion."
    ),
    FAST_20_4(
        label = "20:4",
        fastHours = 20,
        eatHours = 4,
        title = "20:4 The Warrior",
        subtitle = "Advanced",
        description = "20 hours fasting with a 4-hour eating window. Maximizes cellular autophagy, ketone production, and mental clarity."
    );

    companion object {
        fun fromLabel(label: String): FastingProtocol {
            return entries.firstOrNull { it.label == label } ?: FAST_16_8
        }

        fun fromHours(hours: Int): FastingProtocol {
            return entries.firstOrNull { it.fastHours == hours } ?: FAST_16_8
        }
    }
}
