package com.example.bt_transit.di

import com.example.bt_transit.data.remote.GtfsRtClient
import com.example.bt_transit.data.remote.GtfsStaticClient
import com.example.bt_transit.data.remote.WeatherClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideGtfsRtClient(client: OkHttpClient): GtfsRtClient = GtfsRtClient(client)

    @Provides
    @Singleton
    fun provideGtfsStaticClient(client: OkHttpClient): GtfsStaticClient = GtfsStaticClient(client)

    @Provides
    @Singleton
    fun provideWeatherClient(client: OkHttpClient): WeatherClient = WeatherClient(client)
}
