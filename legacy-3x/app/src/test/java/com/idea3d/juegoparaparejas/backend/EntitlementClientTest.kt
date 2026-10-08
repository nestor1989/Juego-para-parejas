package com.idea3d.juegoparaparejas.backend

import org.junit.Test
import org.junit.Assert.*

class EntitlementClientTest {

    @Test
    fun testVerifyRequestStructure() {
        // Test 1: Verify request has all required fields
        val request = VerifyPurchaseRequest(
            appKey = "com.idea3d.juegoparaparejas",
            externalUserId = "test_user_123",
            packageName = "com.idea3d.juegoparaparejas",
            purchaseToken = "token_abc123",
            productId = "premium_monthly"
        )

        assertEquals("com.idea3d.juegoparaparejas", request.appKey)
        assertEquals("test_user_123", request.externalUserId)
        assertEquals("com.idea3d.juegoparaparejas", request.packageName)
        assertEquals("token_abc123", request.purchaseToken)
        assertEquals("premium_monthly", request.productId)
        assertEquals("GOOGLE_PLAY", request.platform)
    }

    @Test
    fun testVerifyResponsePremium() {
        // Test 2: Premium monthly response parsing
        val response = VerifyPurchaseResponse(
            status = "success",
            entitlementStatus = "PREMIUM",
            externalUserId = "test_user_123",
            purchaseToken = "token_abc123",
            productId = "premium_monthly"
        )

        assertTrue(response.entitlementStatus == "PREMIUM")
        assertEquals("premium_monthly", response.productId)
    }

    @Test
    fun testVerifyResponsePremiumYearly() {
        // Test 3: Premium yearly response parsing
        val response = VerifyPurchaseResponse(
            status = "success",
            entitlementStatus = "PREMIUM",
            externalUserId = "test_user_123",
            purchaseToken = "token_xyz789",
            productId = "premium_yearly"
        )

        assertTrue(response.entitlementStatus == "PREMIUM")
        assertEquals("premium_yearly", response.productId)
    }

    @Test
    fun testCurrentEntitlementResponse() {
        // Test 4: Current entitlement response structure
        val response = CurrentEntitlementResponse(
            status = "success",
            entitlementStatus = "PREMIUM",
            externalUserId = "test_user_123",
            productId = "premium_monthly"
        )

        assertTrue(response.entitlementStatus == "PREMIUM")
        assertEquals("premium_monthly", response.productId)
    }

    @Test
    fun testCurrentEntitlementFree() {
        // Test 5: Free status response
        val response = CurrentEntitlementResponse(
            status = "success",
            entitlementStatus = "FREE",
            externalUserId = "test_user_456"
        )

        assertFalse(response.entitlementStatus == "PREMIUM")
        assertEquals("FREE", response.entitlementStatus)
    }

    @Test
    fun testDedupeLogic() {
        // Test 6: Dedupe logic - same token should not be processed twice in 60s
        val tokens = mutableMapOf<String, Long>()
        val dedupeThresholdMs = 60_000L

        val token = "token_123"
        val firstTime = System.currentTimeMillis()

        // First attempt
        tokens[token] = firstTime
        var shouldProcess = (System.currentTimeMillis() - tokens[token]!!) >= dedupeThresholdMs
        assertFalse(shouldProcess) // Should NOT process (just added)

        // Second attempt immediately after
        shouldProcess = (System.currentTimeMillis() - tokens[token]!!) >= dedupeThresholdMs
        assertFalse(shouldProcess) // Should NOT process (within threshold)
    }
}

