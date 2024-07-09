package com.peteragurto.riegoapp.data.network

import retrofit2.Response
import retrofit2.http.GET


interface ArduinoApi {
    @GET("/on")
    suspend fun turnOnRelay(): Response<Void>

    @GET("/off")
    suspend fun turnOffRelay(): Response<Void>

    @GET("/")
    suspend fun getSensorValue(): Response<SensorResponse>
}
