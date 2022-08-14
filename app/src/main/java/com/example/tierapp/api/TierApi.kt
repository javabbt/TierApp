package com.example.tierapp.api

import com.example.tierapp.BuildConfig
import com.example.tierapp.model.VehicleResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TierApi {

    companion object{
        const val GET_VEHICLES = BuildConfig.BASE_URL
        const val TOKEN = BuildConfig.API_TOKEN
    }

    @GET(GET_VEHICLES)
    suspend fun getVehicles(
        @Query("apiKey") apiKey : String = TOKEN
    ) : Response<VehicleResponse>
}