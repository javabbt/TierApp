package com.example.tierapp.repository

import com.example.tierapp.MainRepository
import com.example.tierapp.api.GetVehiclesInteractor
import com.example.tierapp.utils.networkutils.Result
import com.example.tierapp.utils.networkutils.UiResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testing.rule.CoroutineTestRule

class MainRepositoryImplTest {
    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var mainRepository: MainRepository
    private val getVehiclesInteractor: GetVehiclesInteractor = mockk(relaxed = true)

    @Before
    fun setUp() {
        mainRepository = MainRepository.newInstance(getVehiclesInteractor)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `WHEN getting vehicles from MainRepository returns type success`() = runTest {
        coEvery { getVehiclesInteractor.getVehicles() } returns Result.Success(mockk(relaxed = true))
        val res = mainRepository.getVehicles()
        assert(res is UiResult.Success)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `WHEN getting vehicles from MainRepository returns type error`() = runTest {
        coEvery { getVehiclesInteractor.getVehicles() } returns Result.Error(null)
        val res = mainRepository.getVehicles()
        assert(res is UiResult.Error)
    }
}