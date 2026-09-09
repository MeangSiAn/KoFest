package com.mosstis.kofest.feature.festival.home

import com.mosstis.kofest.core.common.AppLanguage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.mosstis.kofest.feature.festival.common.openUrl
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle

@Composable
fun HomeRoute(
    onNavigateToDetail: (AppLanguage, Long) -> Unit,
    onNavigateToList: (regionCode: String?, ongoingOnly: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    // 언어가 바뀌면 상위에서 새 람다를 내려준다. LaunchedEffect 키에 없는 람다는 첫 값으로 굳으므로 최신 값을 읽는다.
    val currentOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)
    val currentOnNavigateToList by rememberUpdatedState(onNavigateToList)

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    is HomeContract.Effect.NavigateToDetail ->
                        currentOnNavigateToDetail(effect.language, effect.contentId)
                    is HomeContract.Effect.NavigateToList ->
                        currentOnNavigateToList(effect.regionCode, effect.ongoingOnly)

                    is HomeContract.Effect.OpenUrl -> context.openUrl(effect.url)
                }
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
