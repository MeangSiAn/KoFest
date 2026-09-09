package com.mosstis.kofest.data.festival.di

import com.mosstis.kofest.data.festival.BuildConfig
import com.mosstis.kofest.data.festival.remote.ApiKeyInterceptor
import com.mosstis.kofest.data.festival.remote.KoFestApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // 서버가 필드를 추가해도 앱이 깨지지 않게 한다.
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor(BuildConfig.KOFEST_API_KEY))
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.KOFEST_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory(APPLICATION_JSON.toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideKoFestApi(retrofit: Retrofit): KoFestApi = retrofit.create(KoFestApi::class.java)

    private const val TIMEOUT_SECONDS = 20L
    private const val APPLICATION_JSON = "application/json"
}
