package com.mosstis.kofest.feature.festival.detail

import com.mosstis.kofest.core.common.AppLanguage
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.mosstis.kofest.feature.festival.common.dial
import com.mosstis.kofest.feature.festival.common.launchOrIgnore
import com.mosstis.kofest.feature.festival.common.openMap

/**
 * 외부 앱 실행(지도·전화·브라우저·공유)은 여기서만 한다.
 * ViewModel 은 "무엇을 열어야 하는지"만 Effect 로 알린다.
 */
@Composable
fun DetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (AppLanguage, Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val currentOnNavigateBack by rememberUpdatedState(onNavigateBack)
    val currentOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    DetailContract.Effect.NavigateBack -> currentOnNavigateBack()

                    is DetailContract.Effect.NavigateToDetail ->
                        currentOnNavigateToDetail(effect.language, effect.contentId)

                    is DetailContract.Effect.OpenMap ->
                        context.openMap(effect.latitude, effect.longitude, effect.label)

                    is DetailContract.Effect.Dial -> context.dial(effect.tel)

                    is DetailContract.Effect.OpenUrl -> context.launchOrIgnore(
                        Intent(Intent.ACTION_VIEW, Uri.parse(effect.url)),
                    )

                    is DetailContract.Effect.ShareFestival -> context.launchOrIgnore(
                        Intent.createChooser(
                            Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, effect.title)
                                putExtra(Intent.EXTRA_TEXT, effect.text)
                            },
                            effect.title,
                        ),
                    )
                }
            }
        }
    }

    DetailScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

