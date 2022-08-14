package com.example.tierapp.api

import com.example.tierapp.BuildConfig
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import testing.rule.ApiMockServerRule
import javax.net.ssl.HttpsURLConnection

class TierApiTest {
    companion object {
        const val BODY_GET_VEHICLES_SUCCESS = "vehicles_json_success.json"
    }

    private lateinit var api: TierApi

    @get:Rule
    val apiMockServer = ApiMockServerRule()

    @Before
    fun setUp() {
        api = apiMockServer.createMockApi()
    }

    @Test
    fun `GIVEN retrieve vehicles from Api and get the size`() = runBlocking {
        apiMockServer.enqueueMockResponse(HttpsURLConnection.HTTP_OK, BODY_GET_VEHICLES_SUCCESS)
        val res = api.getVehicles(BuildConfig.API_TOKEN)
        val body = checkNotNull(res.body())
        Assert.assertEquals(278, body.data.size)
    }


    @Test
    fun `GIVEN retrieve vehicles from Api is success`() = runBlocking {
        apiMockServer.enqueueMockResponse(HttpsURLConnection.HTTP_OK, BODY_GET_VEHICLES_SUCCESS)
        val res = api.getVehicles(BuildConfig.API_TOKEN)
        Assert.assertTrue(res.isSuccessful)
    }

    @Test
    fun `GIVEN retrieve vehicles from Api is Error`() = runBlocking {
        apiMockServer.enqueueMockResponse(HttpsURLConnection.HTTP_INTERNAL_ERROR , null)
        val res = api.getVehicles("wrong_token")
        Assert.assertFalse(res.isSuccessful)
    }
}