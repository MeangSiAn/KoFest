package com.mosstis.kofest.data.festival.remote

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 모든 요청에 `X-API-Key` 를 붙인다.
 *
 * 키는 `local.properties` → `BuildConfig` 로 들어온다. 저장소에 커밋되지 않는다.
 * 쿼리(`?key=`)로도 보낼 수 있지만 URL 은 로그·프록시에 남으므로 헤더를 쓴다.
 */
class ApiKeyInterceptor @Inject constructor(
    private val apiKey: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (apiKey.isBlank()) return chain.proceed(chain.request())

        val request = chain.request().newBuilder()
            .header(HEADER, apiKey)
            .build()
        return chain.proceed(request)
    }

    private companion object {
        const val HEADER = "X-API-Key"
    }
}
