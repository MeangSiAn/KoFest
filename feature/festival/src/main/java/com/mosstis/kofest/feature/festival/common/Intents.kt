package com.mosstis.kofest.feature.festival.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/** 받을 앱이 없으면(지도앱·전화앱이 없는 태블릿 등) 조용히 넘어가되 로그는 남긴다. */
internal fun Context.launchOrIgnore(intent: Intent) {
    try {
        startActivity(intent)
    } catch (notFound: ActivityNotFoundException) {
        android.util.Log.w("KoFestIntent", "받을 앱이 없습니다: ${intent.action} ${intent.data}", notFound)
    }
}

internal fun Context.openUrl(url: String) = launchOrIgnore(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

/** geo: 스킴은 설치된 지도앱이 받는다. 앱 안에 지도를 넣지 않는다 */
internal fun Context.openMap(latitude: Double, longitude: Double, label: String) = launchOrIgnore(
    Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})"),
    ),
)

/** 바로 걸지 않고 다이얼러에 번호만 채운다 */
internal fun Context.dial(tel: String) =
    launchOrIgnore(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel")))
