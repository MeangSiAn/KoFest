package com.mosstis.kofest.feature.festival.plan

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
fun PlanRoute(
    onNavigateToDetail: (AppLanguage, Long) -> Unit,
    onNavigateToPlace: (AppLanguage, Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlanViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentDetail by rememberUpdatedState(onNavigateToDetail)
    val currentPlace by rememberUpdatedState(onNavigateToPlace)

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    is PlanContract.Effect.NavigateToFestival ->
                        currentDetail(effect.language, effect.contentId)
                    is PlanContract.Effect.NavigateToPlace ->
                        currentPlace(effect.language, effect.contentId)
                }
            }
        }
    }

    PlanScreen(uiState = uiState, onAction = viewModel::onAction, modifier = modifier)
}
