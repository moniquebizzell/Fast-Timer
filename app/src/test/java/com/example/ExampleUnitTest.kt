package com.example

import com.example.model.FastingProtocol
import com.example.model.FastingSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FastingUnitTest {

    @Test
    fun fastingProtocols_haveCorrectHours() {
        assertEquals(16, FastingProtocol.FAST_16_8.fastHours)
        assertEquals(8, FastingProtocol.FAST_16_8.eatHours)

        assertEquals(18, FastingProtocol.FAST_18_6.fastHours)
        assertEquals(6, FastingProtocol.FAST_18_6.eatHours)

        assertEquals(20, FastingProtocol.FAST_20_4.fastHours)
        assertEquals(4, FastingProtocol.FAST_20_4.eatHours)
    }

    @Test
    fun targetEndTime_isCalculatedAccurately() {
        val startTime = 1_000_000_000L
        val session16 = FastingSession(
            isActive = true,
            startTimeMillis = startTime,
            protocol = FastingProtocol.FAST_16_8
        )
        val expectedEndTime16 = startTime + (16 * 3600 * 1000L)
        assertEquals(expectedEndTime16, session16.targetEndTimeMillis)

        val session18 = FastingSession(
            isActive = true,
            startTimeMillis = startTime,
            protocol = FastingProtocol.FAST_18_6
        )
        val expectedEndTime18 = startTime + (18 * 3600 * 1000L)
        assertEquals(expectedEndTime18, session18.targetEndTimeMillis)

        val session20 = FastingSession(
            isActive = true,
            startTimeMillis = startTime,
            protocol = FastingProtocol.FAST_20_4
        )
        val expectedEndTime20 = startTime + (20 * 3600 * 1000L)
        assertEquals(expectedEndTime20, session20.targetEndTimeMillis)
    }

    @Test
    fun sessionProgressAndCompletion_areAccurate() {
        val startTime = 10_000_000L
        val durationMillis = 16 * 3600 * 1000L
        val targetEnd = startTime + durationMillis

        val session = FastingSession(
            isActive = true,
            startTimeMillis = startTime,
            protocol = FastingProtocol.FAST_16_8
        )

        // At start: 0% progress, not completed
        assertEquals(0f, session.progressFraction(startTime), 0.001f)
        assertFalse(session.isCompleted(startTime))
        assertEquals(durationMillis, session.remainingMillis(startTime))

        // Halfway (8 hours in): 50% progress
        val halfTime = startTime + (8 * 3600 * 1000L)
        assertEquals(0.5f, session.progressFraction(halfTime), 0.001f)
        assertFalse(session.isCompleted(halfTime))
        assertEquals(8 * 3600 * 1000L, session.remainingMillis(halfTime))

        // Exactly at end time: 100% progress, completed
        assertEquals(1f, session.progressFraction(targetEnd), 0.001f)
        assertTrue(session.isCompleted(targetEnd))
        assertEquals(0L, session.remainingMillis(targetEnd))

        // Past end time: capped progress at 1.0, completed
        val overTime = targetEnd + (2 * 3600 * 1000L)
        assertEquals(1f, session.progressFraction(overTime), 0.001f)
        assertTrue(session.isCompleted(overTime))
    }
}
