package testing.rule

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection


/**
 * This class must be used as Rule field.
 *
 * A MockServer is embedded. It start a new server, and shutdown it on each test
 */
class ApiMockServerRule : TestWatcher() {

    companion object {
        private const val DEFAULT_PORT = 8081
    }

    private lateinit var mockWebServer: MockWebServer
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.FRANCE)

    lateinit var retrofit: Retrofit
        private set

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(
            HttpUrl::class.java,
            JsonDeserializer<HttpUrl?> { json, _, _ -> json?.asJsonPrimitive?.asString?.toHttpUrl() })
        .registerTypeAdapter(
            HttpUrl::class.java,
            JsonSerializer<HttpUrl?> { src, _, context -> context.serialize(src.toString()) }
        )
        .registerTypeAdapter(
            Date::class.java,
            JsonDeserializer { json, _, _ -> json?.asString?.let { dateFormat.parse(it) } }
        ).registerTypeAdapter(
            Date::class.java,
            JsonSerializer<Date?> { src, _, context ->
                context.serialize(dateFormat.format(src))
            }
        )
        .create()

    override fun starting(description: Description) {
        super.starting(description)
        mockWebServer = MockWebServer()
        mockWebServer.start(InetAddress.getLoopbackAddress(), DEFAULT_PORT)
        println("\n--------------------")
        println(description?.methodName)
        retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor {
                println(it)
            }.apply {
                this.level = HttpLoggingInterceptor.Level.BODY
            }).build())
            .baseUrl(mockWebServer.url("/"))
            .build()
    }

    override fun finished(description: Description) {
        super.finished(description)
        print("\n\n")
        mockWebServer.shutdown()
    }

    /**
     * Enqueue a new response in the mock server
     *
     * This methods works under Unit Test
     *
     * @param responseCode Must be define with [HttpsURLConnection] code
     * @param bodyResourcePath The path is automatically prefix with `src/test/res/json/`. Default is null
     */
    fun enqueueMockResponse(responseCode: Int, bodyResourcePath: String? = null) {
        mockWebServer.enqueue(MockResponse().apply {
            this.setHeader("Content-Type", "application/json; charset=UTF-8")
            this.setResponseCode(responseCode)
            bodyResourcePath?.run {
                InputStreamReader(FileInputStream(File("src/sharedTest/res/json/$this"))).use { it.readText() }
            }?.also { this.setBody(it) }
        })
    }

    /**
     * Enqueue a new [MockResponse] from a builder
     *
     * @param init Default is empty [MockResponse] builder
     */
    fun enqueueMockResponse(init: (MockResponse.() -> Unit) = {}) {
        mockWebServer.enqueue(MockResponse().apply(init))
    }

    fun setDispatcher(dispatcher: okhttp3.mockwebserver.Dispatcher) {
        mockWebServer.dispatcher = dispatcher
    }

    fun getBaseUrl(): String {
        return mockWebServer.url("/").toString()
    }


    fun createGsonResponse(remote: Any): String {
        return gson.toJson(remote).toString()
    }

    inline fun <reified Api> createMockApi() = this.retrofit.create<Api>()
}