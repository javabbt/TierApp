package com.example.tierapp.utils.networkutils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Wrap a suspending API [apiCall] in try/catch. In case an exception is thrown, maps the [Exception] into
 * more meaningful errors.
 * It does not seem it is doing much with respect to error deserialization but shows how we can do that here.
 *
 * This is taken off of:
 * https://medium.com/@douglas.iacovelli/how-to-handle-errors-with-retrofit-and-coroutines-33e7492a912
 */
suspend fun <T : Any> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> Response<T>
): Result<T> {
    return withContext(dispatcher) {
        try {
            val response = apiCall.invoke()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.Success(body)
            } else {
                // Response with empty body, unlikely to happen but definitely an issue
                Result.Error(IllegalStateException())
            }
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> {
                    // Would be nicer with [NoNetworkConnectivityException] itself, but we can't have it as a dependency
                    Result.Error(throwable)
                }
                is HttpException -> {
                    // We could do better and deserialize the response into another [ErrorResponse] class (based on throwable.code())
                    Result.Error(throwable)
                }
                else -> {
                    Result.Error(IllegalStateException())
                }
            }
        }
    }
}