package com.miempresa.veterinaria.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://randomuser.me/"

    val instancia: RandomUserService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Asegúrate de que no haya un espacio o un error tipográfico aquí:
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RandomUserService::class.java)
    }
}