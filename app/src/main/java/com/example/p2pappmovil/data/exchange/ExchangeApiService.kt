package com.example.p2pappmovil.data.exchange

import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeApiService {
    @GET("latest/{base}")
    suspend fun getLatestRates(@Path("base") base: String): ExchangeResponse
}
