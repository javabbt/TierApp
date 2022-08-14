package com.example.tierapp.api

import com.example.tierapp.utils.DispatcherProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testing.rule.ApiMockServerRule
import testing.rule.CoroutineTestRule
import javax.net.ssl.HttpsURLConnection

@OptIn(ExperimentalCoroutinesApi::class)
class GetVehiclesInteractorTest {

    @get:Rule
    val apiMockServer = ApiMockServerRule()

    private lateinit var api: TierApi
    private lateinit var sut : GetVehiclesInteractor
    private  val dispatcherProvider:DispatcherProvider = mockk(relaxed = true)


    @Before
    fun setup() {
        api = apiMockServer.createMockApi()
        every { dispatcherProvider.io } returns coroutineTestRule.testDispatcher
        sut = GetVehiclesInteractor(api, dispatcherProvider)
    }

    companion object {
        const val BODY_GET_VEHICLES_SUCCESS = "vehicles_json_success.json"
    }

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @Test
    fun `WHEN getting vehicles from vehicleinteractor returns type success`() = runTest {
        apiMockServer.enqueueMockResponse(
            HttpsURLConnection.HTTP_OK,
            BODY_GET_VEHICLES_SUCCESS
        )
        val res = sut.getVehicles()
        assertNotNull(res.data)
    }

}