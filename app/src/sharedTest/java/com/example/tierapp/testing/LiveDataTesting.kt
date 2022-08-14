package testing

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun <T> LiveData<T>.getBlockingValue(timeout: Long = 2L): T? {
    var value: T? = null
    val latch = CountDownLatch(1)
    val observer = Observer<T> {
        value = it
        latch.countDown()
    }
    observeForever(observer)
    if (!latch.await(timeout, TimeUnit.SECONDS)) {
        removeObserver(observer)
        throw TimeoutException("LiveData value was never set.")
    }
    removeObserver(observer)
    return value
}