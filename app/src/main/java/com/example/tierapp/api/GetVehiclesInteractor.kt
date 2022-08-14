package com.example.tierapp.api

import com.example.tierapp.utils.DispatcherProvider
import com.example.tierapp.utils.networkutils.safeApiCall
import javax.inject.Inject

class GetVehiclesInteractor @Inject constructor(
    private val api: TierApi,
    private val coroutineDispatcher: DispatcherProvider
) {

    suspend fun getVehicles() = safeApiCall(coroutineDispatcher.io, apiCall = {
        api.getVehicles()
    })

}