package com.idea3d.juegoparaparejas.backend

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Call

interface EntitlementApi {
    
    @POST("/api/v1/entitlements/verify")
    fun verifyPurchase(
        @Header("X-App-Key") appKey: String,
        @Body request: VerifyPurchaseRequest
    ): Call<VerifyPurchaseResponse>
    
    @GET("/api/v1/entitlements/current")
    fun getCurrentEntitlement(
        @Header("X-App-Key") appKey: String,
        @Query("appKey") queryAppKey: String,
        @Query("externalUserId") externalUserId: String
    ): Call<CurrentEntitlementResponse>
}

