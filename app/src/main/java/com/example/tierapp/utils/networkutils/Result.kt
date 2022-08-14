package com.example.tierapp.utils.networkutils

import java.lang.Exception

sealed class Result<T>(val data: T?, val exception: Exception?) {
    class Success<T>(data: T) : Result<T>(data, null)
    class Error<T>(exception: Exception?) : Result<T>(null, exception)
}