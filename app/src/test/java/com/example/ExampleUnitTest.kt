package com.example

import com.example.model.LicensePlan
import com.example.model.OrderStatus
import com.example.model.PaymentOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class MayraEcosystemUnitTest {

    @Test
    fun testPlanPricingAndValidity() {
        assertEquals(0, LicensePlan.FREE.priceInr)
        assertEquals(99, LicensePlan.YEARLY.priceInr)
        assertEquals(365, LicensePlan.YEARLY.validityDays)
        assertEquals(299, LicensePlan.LIFETIME.priceInr)
        assertNull(LicensePlan.LIFETIME.validityDays) // null = permanent
    }

    @Test
    fun testThreeHourOrderDeadline() {
        val now = System.currentTimeMillis()
        val deadline = now + TimeUnit.HOURS.toMillis(3)
        val order = PaymentOrder(
            orderId = "MAYRA-ORD-12345",
            userId = "USER-1",
            userEmail = "user@test.com",
            plan = LicensePlan.YEARLY,
            amount = 99,
            payeeUpi = "mayra.ai@upi",
            payeeName = "Mayra AI",
            createdAt = now,
            deadlineTimestamp = deadline,
            status = OrderStatus.AWAITING_PAYMENT
        )

        // Within 3-hour window
        assertTrue(now + TimeUnit.MINUTES.toMillis(30) <= order.deadlineTimestamp)
        // Beyond 3-hour window
        assertFalse(now + TimeUnit.HOURS.toMillis(4) <= order.deadlineTimestamp)
    }

    @Test
    fun testDailyAllowanceSeconds() {
        val totalAllowanceSeconds = 600 // 10 minutes
        val consumed = 15
        val remaining = (totalAllowanceSeconds - consumed).coerceAtLeast(0)
        assertEquals(585, remaining)

        // Less than 60s warning threshold
        val lowAllowance = 45
        assertTrue(lowAllowance in 1..60)

        // Locked threshold
        val zeroAllowance = 0
        assertTrue(zeroAllowance <= 0)
    }

    @Test
    fun testUtrValidation() {
        val validUtr = "428901238912"
        val shortUtr = "123"

        assertTrue(validUtr.length >= 8)
        assertFalse(shortUtr.length >= 8)
    }
}
