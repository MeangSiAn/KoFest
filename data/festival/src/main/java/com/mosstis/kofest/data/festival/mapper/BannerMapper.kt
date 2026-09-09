package com.mosstis.kofest.data.festival.mapper

import com.mosstis.kofest.data.festival.remote.BannerDto
import com.mosstis.kofest.domain.festival.model.Banner

/**
 * 이미지가 없는 배너만 버린다. 글(kicker/title/subtitle)은 통째로 없을 수 있다 —
 * 실제 응답에 이미지와 alt 만 있는 배너가 있다. **순서는 바꾸지 않는다.**
 */
internal fun BannerDto.toDomainOrNull(baseUrl: String): Banner? {
    val image = imageUrl?.takeIf { it.isNotBlank() } ?: return null
    return Banner(
        id = id.toString(),
        imageUrl = absolute(image, baseUrl),
        imageAlt = imageAlt?.takeIf { it.isNotBlank() },
        kicker = kicker?.takeIf { it.isNotBlank() },
        title = title?.takeIf { it.isNotBlank() },
        subtitle = subtitle?.takeIf { it.isNotBlank() },
        link = when (linkType) {
            "url" -> linkValue?.takeIf { it.isNotBlank() }?.let { Banner.Link.Url(withScheme(it)) }
            "festival" -> linkValue?.toLongOrNull()?.let { Banner.Link.FestivalDetail(it) }
            else -> null
        },
    )
}

/** `/media/...` 는 API 서버 기준 상대 경로다. */
private fun absolute(path: String, baseUrl: String): String =
    if (path.startsWith("http://") || path.startsWith("https://")) path
    else baseUrl.trimEnd('/') + "/" + path.trimStart('/')

/** 관리자가 `www.naver.com` 처럼 스킴 없이 넣는다. 그대로 Intent 에 주면 열리지 않는다. */
private fun withScheme(url: String): String =
    if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
