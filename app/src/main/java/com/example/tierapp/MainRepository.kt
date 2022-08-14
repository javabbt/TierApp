package com.example.tierapp

import com.example.tierapp.api.GetVehiclesInteractor
import com.example.tierapp.model.Data
import com.example.tierapp.utils.networkutils.Result
import com.example.tierapp.utils.networkutils.UiResult
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val getVehiclesInteractor: GetVehiclesInteractor
) : MainRepository {
    override suspend fun getVehicles(): UiResult<List<Data>> {
        return when (
            val result = getVehiclesInteractor.getVehicles()
        ) {
            is Result.Success -> UiResult.Success(result.data!!.data)
            is Result.Error -> UiResult.Error(result.exception)
        }
    }
}

interface MainRepository {
    companion object {
        fun newInstance(
            getVehiclesInteractor: GetVehiclesInteractor
        ): MainRepository {
            return MainRepositoryImpl(getVehiclesInteractor)
        }
    }

    suspend fun getVehicles(): UiResult<List<Data>>
}