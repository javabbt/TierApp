package com.example.tierapp.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface DispatcherProvider {

    companion object {

        suspend inline fun <reified T, R> T.runWithContext(
            context: CoroutineDispatcher,
            noinline block: suspend T.() -> R
        ): R {
            return this.run {
                withContext(context) {
                    block(this@runWithContext)
                }
            }
        }

    }

    val main: CoroutineDispatcher
        get() = Dispatchers.Main

    val default: CoroutineDispatcher
        get() = Dispatchers.Default

    val io: CoroutineDispatcher
        get() = Dispatchers.IO

}

class DefaultDispatcherProvider : DispatcherProvider