package com.mosstis.kofest.feature.festival.intro

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.designsystem.component.IntroGrainAlpha
import com.mosstis.kofest.core.designsystem.component.IntroGrainSpacing
import com.mosstis.kofest.core.designsystem.component.drawDiagonalGrain
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.core.designsystem.theme.KoFestDimens
import com.mosstis.kofest.core.designsystem.theme.KoFestFonts
import com.mosstis.kofest.core.designsystem.theme.KoFestTheme
import com.mosstis.kofest.core.ui.strings.strings

/**
 * 인트로 — 스플래시와 언어 선택.
 *
 * 두 단계가 같은 배경을 공유하므로 화면을 나누지 않고 [IntroContract.Phase] 로만 바꾼다.
 * 화면을 나누면 전환할 때 배경이 한 번 깜빡인다.
 */
@Composable
fun IntroScreen(
    uiState: IntroContract.State,
    onAction: (IntroContract.Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    // CSS 168deg — 거의 수직이면서 살짝 기운 방향
                    0.00f to KoFestColors.IntroGradientTop,
                    0.52f to KoFestColors.IntroGradientMid,
                    1.00f to KoFestColors.IntroGradientBottom,
                    start = Offset.Zero,
                    end = Offset(x = 220f, y = Float.POSITIVE_INFINITY),
                ),
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawDiagonalGrain(spacing = IntroGrainSpacing, alpha = IntroGrainAlpha)
        }

        when (uiState.phase) {
            IntroContract.Phase.SPLASH -> SplashContent()
            IntroContract.Phase.LANGUAGE -> LanguageContent(uiState = uiState, onAction = onAction)
        }
    }
}

/** 우측에 잘린 낙관. 목록에서 뺐던 도장 모티프를 여기 한 번만 쓴다. */
@Composable
private fun BoxScopeSeal(
    hanja: String,
    topPadding: Int,
    size: Int,
    fontSize: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(top = topPadding.dp)
            .offset(x = 26.dp)
            .size(size.dp)
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = hanja,
            fontFamily = KoFestFonts.NanumMyeongjo,
            fontSize = fontSize.sp,
            color = Color.White.copy(alpha = 0.13f),
        )
    }
}

@Composable
private fun SplashContent() {
    val s = strings().intro

    Box(modifier = Modifier.fillMaxSize()) {
        BoxScopeSeal(
            hanja = "祭",
            topPadding = 96,
            size = 150,
            fontSize = 76,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(horizontal = KoFestDimens.IntroMargin)
                .padding(bottom = 30.dp),
        ) {
            Text(
                text = strings().app.name,
                style = KoFestTheme.type.introMark,
                color = Color.White,
            )
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .width(30.dp)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.4f)),
            )
            Spacer(Modifier.height(18.dp))
            Text(text = s.splashHeadline, style = KoFestTheme.type.introHeadline, color = Color.White)
            Spacer(Modifier.height(12.dp))
            Text(
                text = s.splashSub,
                style = KoFestTheme.type.introSub,
                color = Color.White.copy(alpha = 0.66f),
            )

            Spacer(Modifier.height(34.dp))
            DotLoader()
            Spacer(Modifier.height(16.dp))
            Text(
                text = s.dataSource,
                style = KoFestTheme.type.introFoot,
                color = Color.White.copy(alpha = 0.4f),
            )
        }
    }
}

/**
 * 점 3개. 진행률 막대를 두지 않는다 — 얼마나 걸릴지 모르면서 그리면 거짓말이 된다.
 * 점은 "돌고 있다"만 말하고 남은 시간을 약속하지 않는다.
 */
@Composable
private fun DotLoader() {
    val transition = rememberInfiniteTransition(label = "dots")

    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(DOT_COUNT) { index ->
            val alpha by transition.animateFloat(
                initialValue = DOT_MIN_ALPHA,
                targetValue = DOT_MIN_ALPHA,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = DOT_CYCLE_MS
                        DOT_MIN_ALPHA at index * DOT_STAGGER_MS
                        1f at index * DOT_STAGGER_MS + DOT_STAGGER_MS
                        DOT_MIN_ALPHA at index * DOT_STAGGER_MS + DOT_STAGGER_MS * 2
                    },
                    repeatMode = RepeatMode.Restart,
                ),
                label = "dot$index",
            )
            Box(
                Modifier
                    .size(5.dp)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(
                        if (index == 0) KoFestColors.BannerKicker else Color.White.copy(alpha = 0.45f),
                    ),
            )
        }
    }
}

@Composable
private fun LanguageContent(
    uiState: IntroContract.State,
    onAction: (IntroContract.Action) -> Unit,
) {
    val s = strings().intro

    Box(modifier = Modifier.fillMaxSize()) {
        BoxScopeSeal(
            hanja = "語",
            topPadding = 52,
            size = 120,
            fontSize = 64,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .navigationBarsPadding()
                .padding(horizontal = KoFestDimens.IntroMargin),
        ) {
            Text(
                text = s.langKicker,
                style = KoFestTheme.type.introKicker,
                color = KoFestColors.BannerKicker,
            )
            Spacer(Modifier.height(10.dp))
            Text(text = s.langTitle, style = KoFestTheme.type.introTitle, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text(
                text = s.langDesc,
                style = KoFestTheme.type.introDesc,
                color = Color.White.copy(alpha = 0.58f),
            )
            Spacer(Modifier.height(26.dp))

            LanguageOption(
                name = "한국어",
                description = s.langKoLabel,
                count = uiState.counts?.ko,
                selected = uiState.selected == AppLanguage.KO,
                onClick = { onAction(IntroContract.Action.SelectLanguage(AppLanguage.KO)) },
            )
            Spacer(Modifier.height(10.dp))
            LanguageOption(
                name = "English",
                description = s.langEnLabel,
                count = uiState.counts?.en,
                selected = uiState.selected == AppLanguage.EN,
                onClick = { onAction(IntroContract.Action.SelectLanguage(AppLanguage.EN)) },
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = s.langNote,
                style = KoFestTheme.type.introNote,
                color = Color.White.copy(alpha = 0.44f),
            )

            Spacer(Modifier.height(26.dp))
            Text(
                text = s.start,
                style = KoFestTheme.type.introCta,
                color = KoFestColors.Jaju,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .clickable { onAction(IntroContract.Action.Start) }
                    .padding(vertical = 15.dp),
            )
            // 되돌릴 수 있다는 걸 알면 빨리 고른다.
            Text(
                text = s.changeAnytime,
                style = KoFestTheme.type.introSkip,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
        }
    }
}

/**
 * 언어 카드. 건수를 숫자로 그대로 보여준다 —
 * "일부만 제공됩니다"보다 "903 vs 235"가 판단을 돕는다.
 *
 * [count] 가 null 이면 숫자를 아예 그리지 않는다. "0건"은 앱이 비었다는 오해를 만든다.
 */
@Composable
private fun LanguageOption(
    name: String,
    description: String,
    count: Int?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) Color.White else Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = if (selected) 1f else 0.14f))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = KoFestTheme.type.introOptionName,
                color = if (selected) KoFestColors.Ink else Color.White,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = description,
                style = KoFestTheme.type.introOptionDesc,
                color = if (selected) KoFestColors.Muted else Color.White.copy(alpha = 0.55f),
            )
        }
        if (count != null) {
            Spacer(Modifier.width(12.dp))
            Text(
                text = count.toString(),
                style = KoFestTheme.type.introOptionCount,
                color = if (selected) KoFestColors.Jaju else Color.White.copy(alpha = 0.6f),
            )
        }
    }
}

private const val DOT_COUNT = 3
private const val DOT_MIN_ALPHA = 0.45f
private const val DOT_STAGGER_MS = 260
private const val DOT_CYCLE_MS = 1_400
