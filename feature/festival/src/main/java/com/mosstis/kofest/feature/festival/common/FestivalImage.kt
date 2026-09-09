package com.mosstis.kofest.feature.festival.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.mosstis.kofest.core.designsystem.component.BlankImage

/**
 * 축제 사진. 없거나 실패하면 회색 네모가 아니라 자주색 한자 면을 둔다.
 *
 * 로딩 중에도 같은 면을 보여준다 — 자리가 비었다가 채워지면 목록이 흔들린다.
 *
 * @param url 먼저 시도할 URL. 배너·카드는 원본([com.mosstis.kofest.domain.festival.model.Festival.imageUrl])을 준다
 * @param fallbackUrl [url] 이 실패했을 때 대신 쓸 URL. 원본은 유도한 값이라 없을 수 있어 썸네일을 준다
 * @param contentDescription 스크린리더용. 축제 사진은 제목이 옆에 있어 null 이지만 배너는 alt 를 준다
 */
@Composable
fun FestivalImage(
    url: String?,
    title: String,
    modifier: Modifier = Modifier,
    hanjaSize: TextUnit = 42.sp,
    fallbackUrl: String? = null,
    contentDescription: String? = null,
) {
    val hanja = Hanja.forTitle(title)
    val primary = url?.takeIf { it.isNotBlank() } ?: fallbackUrl?.takeIf { it.isNotBlank() }
    val fallback = fallbackUrl?.takeIf { it.isNotBlank() && it != primary }

    if (primary == null) {
        BlankImage(hanja = hanja, modifier = modifier, hanjaSize = hanjaSize)
        return
    }

    Box(modifier = modifier) {
        SubcomposeAsyncImage(
            model = primary,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = { BlankImage(hanja = hanja, modifier = Modifier.fillMaxSize(), hanjaSize = hanjaSize) },
            error = {
                if (fallback != null) {
                    SubcomposeAsyncImage(
                        model = fallback,
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = { BlankImage(hanja = hanja, modifier = Modifier.fillMaxSize(), hanjaSize = hanjaSize) },
                        error = { BlankImage(hanja = hanja, modifier = Modifier.fillMaxSize(), hanjaSize = hanjaSize) },
                    )
                } else {
                    BlankImage(hanja = hanja, modifier = Modifier.fillMaxSize(), hanjaSize = hanjaSize)
                }
            },
        )
    }
}
