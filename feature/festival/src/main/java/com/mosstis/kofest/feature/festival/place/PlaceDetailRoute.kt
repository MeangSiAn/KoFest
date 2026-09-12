package com.mosstis.kofest.feature.festival.place

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
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.feature.festival.common.dial
import com.mosstis.kofest.feature.festival.common.openMap

@Composable
fun PlaceDetailRoute(
    onNavigateBack: () -> Unit,
    onNavigateToPlace: (AppLanguage, Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentBack by rememberUpdatedState(onNavigateBack)
    val currentPlace by rememberUpdatedState(onNavigateToPlace)

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    PlaceDetailContract.Effect.NavigateBack -> currentBack()
                    is PlaceDetailContract.Effect.NavigateToPlace ->
                        currentPlace(effect.language, effect.contentId)
                    is PlaceDetailContract.Effect.OpenMap ->
                        context.openMap(effect.latitude, effect.longitude, effect.label)
                    is PlaceDetailContract.Effect.Dial -> context.dial(effect.tel)
                }
            }
        }
    }

    PlaceDetailScreen(uiState = uiState, onAction = viewModel::onAction, modifier = modifier)
}
