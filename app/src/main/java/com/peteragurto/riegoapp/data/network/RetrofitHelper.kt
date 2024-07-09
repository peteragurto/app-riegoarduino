package com.peteragurto.riegoapp.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    @Volatile
    private var retrofit: Retrofit? = null

    fun getRetrofitInstance(baseUrl: String): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build().also { retrofit = it }
        }
    }
}