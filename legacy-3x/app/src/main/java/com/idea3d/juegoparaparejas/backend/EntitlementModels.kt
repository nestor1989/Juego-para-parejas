package com.idea3d.juegoparaparejas.backend

import com.google.gson.annotations.SerializedName

// Request models
data class VerifyPurchaseRequest(
    @SerializedName("appKey")
    val appKey: String,
    @SerializedName("externalUserId")
    val externalUserId: String,
    @SerializedName("packageName")
    val packageName: String,
    @SerializedName("purchaseToken")
    val purchaseToken: String,
    @SerializedName("productId")
    val productId: String,
    @SerializedName("basePlanId")
    val basePlanId: String? = null,
    @SerializedName("platform")
    val platform: String = "GOOGLE_PLAY"
)

// Response models
data class VerifyPurchaseResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("entitlementStatus")
    val entitlementStatus: String, // PREMIUM or FREE
    @SerializedName("externalUserId")
    val externalUserId: String,
    @SerializedName("purchaseToken")
    val purchaseToken: String,
    @SerializedName("productId")
    val productId: String,
    @SerializedName("expiresAt")
    val expiresAt: Long? = null
)

data class CurrentEntitlementResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("entitlementStatus")
    val entitlementStatus: String, // PREMIUM or FREE
    @SerializedName("externalUserId")
    val externalUserId: String,
    @SerializedName("productId")
    val productId: String? = null,
    @SerializedName("expiresAt")
    val expiresAt: Long? = null,
    @SerializedName("lastSync")
    val lastSync: Long? = null
)

data class ApiError(
    @SerializedName("error")
    val error: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("code")
    val code: Int? = null
)

