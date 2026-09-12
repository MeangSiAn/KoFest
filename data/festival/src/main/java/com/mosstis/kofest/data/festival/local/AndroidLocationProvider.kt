package com.mosstis.kofest.data.festival.local

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import android.os.SystemClock
import com.mosstis.kofest.domain.festival.repository.GeoPoint
import com.mosstis.kofest.domain.festival.repository.LocationProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * 프레임워크 `LocationManager` 로 읽는다. Play 서비스 위치 라이브러리를 넣지 않는다 —
 * 10km 반경을 고르는 데 정밀도가 필요 없고, 의존성 하나가 늘 이유가 없다.
 *
 * 웹(`/pick`)과 같은 조건이다: 5분 안의 마지막 위치면 그대로 쓰고, 없으면 8초 안에 한 번 잡는다.
 */
@Singleton
class AndroidLocationProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : LocationProvider {

    private val manager: LocationManager?
        get() = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    override fun hasPermission(): Boolean =
        granted(Manifest.permission.ACCESS_FINE_LOCATION) || granted(Manifest.permission.ACCESS_COARSE_LOCATION)

    @SuppressLint("MissingPermission")
    override suspend fun current(): GeoPoint? {
        if (!hasPermission()) return null
        val manager = manager ?: return null
        val providers = PROVIDERS.filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
        if (providers.isEmpty()) return null

        providers
            .mapNotNull { manager.getLastKnownLocation(it) }
            .filter { it.ageMillis() <= MAX_AGE_MILLIS }
            .maxByOrNull { it.time }
            ?.let { return it.toGeoPoint() }

        return withTimeoutOrNull(TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { continuation ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val signal = CancellationSignal()
                    continuation.invokeOnCancellation { signal.cancel() }
                    manager.getCurrentLocation(providers.first(), signal, MainExecutor) { location ->
                        if (continuation.isActive) continuation.resume(location?.toGeoPoint())
                    }
                } else {
                    val listener = LocationListener { location ->
                        if (continuation.isActive) continuation.resume(location.toGeoPoint())
                    }
                    continuation.invokeOnCancellation { manager.removeUpdates(listener) }
                    @Suppress("DEPRECATION")
                    manager.requestSingleUpdate(providers.first(), listener, Looper.getMainLooper())
                }
            }
        }
    }

    private fun granted(permission: String): Boolean =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun Location.ageMillis(): Long =
        (SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos) / 1_000_000

    private fun Location.toGeoPoint() = GeoPoint(latitude, longitude)

    private object MainExecutor : Executor {
        private val handler = android.os.Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            handler.post(command)
        }
    }

    private companion object {
        /** 네트워크가 먼저다 — 실내에서도 잡히고 10km 를 고르기엔 충분하다 */
        val PROVIDERS = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
        const val MAX_AGE_MILLIS = 5 * 60 * 1000L
        const val TIMEOUT_MILLIS = 8_000L
    }
}
