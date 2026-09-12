package com.mosstis.kofest.feature.festival.list

import com.mosstis.kofest.core.common.AppLanguage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun ListRoute(
    onNavigateToDetail: (AppLanguage, Long) -> Unit,
    onNavigateToPlace: (AppLanguage, Long) -> Unit,
    /** 상단 검색 아이콘. 검색은 탭이 아니라 검색창에서 들어간다 */
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)
    val currentOnNavigateToPlace by rememberUpdatedState(onNavigateToPlace)

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    is ListContract.Effect.NavigateToDetail ->
                        currentOnNavigateToDetail(effect.language, effect.contentId)

                    is ListContract.Effect.NavigateToPlace ->
                        currentOnNavigateToPlace(effect.language, effect.contentId)
                }
            }
        }
    }

    ListScreen(
        onSearch = onSearch,
        uiState = uiState,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}
