package com.example.tierapp.utils

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import org.junit.Test

class DefaultDispatcherProviderTest {

    private val sut: DispatcherProvider = mockk(relaxed = true)

    @Test
    fun `VERIFY dispatcher provider types`() {
        every { sut.io } returns Dispatchers.IO
        every { sut.default } returns Dispatchers.Default
        every { sut.main } returns Dispatchers.Main
        sut.io
        sut.main
        sut.default
        verify { sut.io }
        verify { sut.default }
        verify { sut.main }
    }

}