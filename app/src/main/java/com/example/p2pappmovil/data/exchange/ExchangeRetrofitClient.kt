package com.example.p2pappmovil.data.exchange

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ExchangeRetrofitClient {
    private const val BASE_URL = "https://open.er-api.com/v6/"

    val service: ExchangeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeApiService::class.java)
    }
}
