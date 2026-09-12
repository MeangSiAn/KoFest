package com.mosstis.kofest.feature.festival.magazine

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
import com.mosstis.kofest.core.common.AppLanguage

@Composable
fun StoryRoute(
    onNavigateBack: () -> Unit,
    onNavigateToFestival: (AppLanguage, Long) -> Unit,
    onNavigateToPlace: (AppLanguage, Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentBack by rememberUpdatedState(onNavigateBack)
    val currentFestival by rememberUpdatedState(onNavigateToFestival)
    val currentPlace by rememberUpdatedState(onNavigateToPlace)

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    StoryContract.Effect.NavigateBack -> currentBack()
                    is StoryContract.Effect.NavigateToFestival ->
                        currentFestival(effect.language, effect.contentId)
                    is StoryContract.Effect.NavigateToPlace ->
                        currentPlace(effect.language, effect.contentId)
                }
            }
        }
    }

    StoryScreen(uiState = uiState, onAction = viewModel::onAction, modifier = modifier)
}
