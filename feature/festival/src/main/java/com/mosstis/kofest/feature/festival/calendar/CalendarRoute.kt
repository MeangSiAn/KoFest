package com.mosstis.kofest.feature.festival.calendar

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
fun CalendarRoute(
    onNavigateToDetail: (AppLanguage, Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    is CalendarContract.Effect.NavigateToDetail ->
                        currentOnNavigateToDetail(effect.language, effect.contentId)
                }
            }
        }
    }

    CalendarScreen(uiState = uiState, onAction = viewModel::onAction, modifier = modifier)
}
