package com.mosstis.kofest.feature.festival.pick

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
fun PickRoute(
    onNavigateToPlace: (AppLanguage, Long) -> Unit,
    onNavigateToList: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PickViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentPlace by rememberUpdatedState(onNavigateToPlace)
    val currentList by rememberUpdatedState(onNavigateToList)

    // 권한 대화상자는 기기 쪽 일이라 여기서만 띄운다. 대략 위치만 허락해도 된다 — 10km 를 고르는 데 충분하다.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        viewModel.onAction(PickContract.Action.PermissionResult(granted = result.values.any { it }))
    }

    LaunchedEffect(viewModel) { viewModel.onAction(PickContract.Action.ScreenStarted) }

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    PickContract.Effect.RequestPermission -> permissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    )
                    is PickContract.Effect.NavigateToPlace -> currentPlace(effect.language, effect.contentId)
                    PickContract.Effect.NavigateToList -> currentList()
                }
            }
        }
    }

    PickScreen(uiState = uiState, onAction = viewModel::onAction, modifier = modifier)
}
