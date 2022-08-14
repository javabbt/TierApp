package com.example.tierapp.di

import android.app.Application
import android.content.Context
import com.example.tierapp.BuildConfig
import com.example.tierapp.BuildConfig.BASE_URL
import com.example.tierapp.MainRepository
import com.example.tierapp.api.GetVehiclesInteractor
import com.example.tierapp.api.TierApi
import com.example.tierapp.utils.DefaultDispatcherProvider
import com.example.tierapp.utils.DispatcherProvider
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val MAX_SIZE_OF_CACHE = 5 * 1024 * 1024L // 5 MB for images and request

    @Singleton
    @Provides
    fun provideMoshi(): Gson {
        return Gson()
    }

    @Singleton
    @Provides
    fun provideOkHttp(@ApplicationContext androidApplication: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .callTimeout(0L, TimeUnit.SECONDS)
            .readTimeout(0L, TimeUnit.SECONDS)
            .writeTimeout(0L, TimeUnit.SECONDS)
            .cache(Cache(androidApplication.cacheDir, MAX_SIZE_OF_CACHE))
            .apply {
                if (BuildConfig.DEBUG) this.addInterceptor(
                    HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                )
            }.build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .baseUrl(BASE_URL)
            .build()
    }

    @Singleton
    @Provides
    fun provideDispatcher(): DispatcherProvider {
        return DefaultDispatcherProvider()
    }

    @Singleton
    @Provides
    fun provideImageApi(retrofit: Retrofit) = retrofit.create(TierApi::class.java)

    @Singleton
    @Provides
    fun provideGetVehiclesInteractor(
        api: TierApi,
        coroutineDispatcher: DispatcherProvider
    ): GetVehiclesInteractor {
        return GetVehiclesInteractor(api, coroutineDispatcher)
    }

    @Singleton
    @Provides
    fun provideMainRepository(
        getVehiclesInteractor: GetVehiclesInteractor
    ): MainRepository {
        return MainRepository.newInstance(getVehiclesInteractor)
    }
}