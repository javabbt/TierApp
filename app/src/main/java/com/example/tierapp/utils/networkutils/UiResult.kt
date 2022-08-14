package com.example.tierapp.utils.networkutils

import java.lang.Exception

sealed class UiResult<out t : Any> {
    data class Success<out T : Any>(val output  : T) : UiResult<T>()
    data class Error(val exception: Exception?) : UiResult<Nothing>()
    object Loading : UiResult<Nothing>()
}