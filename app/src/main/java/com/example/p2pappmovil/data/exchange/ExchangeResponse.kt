package com.example.p2pappmovil.data.exchange

import com.google.gson.annotations.SerializedName

data class ExchangeResponse(
    @SerializedName("result") val result: String,
    @SerializedName("base_code") val baseCode: String,
    @SerializedName("rates") val rates: Map<String, Double>,
    @SerializedName("time_last_update_unix") val timeLastUpdateUnix: Long
)
