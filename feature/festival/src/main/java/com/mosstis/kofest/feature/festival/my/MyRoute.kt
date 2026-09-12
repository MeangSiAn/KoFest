package com.mosstis.kofest.feature.festival.my

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil3.SingletonImageLoader
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.ui.strings.strings
import com.mosstis.kofest.feature.festival.common.openUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 기기 쪽 일(캐시 크기·비우기, 버전, 토스트, 외부 URL)은 여기서만 한다.
 * ViewModel 은 "무엇을 해야 하는지"만 Effect 로 알린다.
 */
@Composable
fun MyRoute(
    onNavigateToDetail: (AppLanguage, Long) -> Unit,
    onNavigateToList: () -> Unit,
    /** 상단 검색 아이콘. 검색은 탭이 아니라 검색창에서 들어간다 */
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val currentOnNavigateToDetail by rememberUpdatedState(onNavigateToDetail)
    val currentOnNavigateToList by rememberUpdatedState(onNavigateToList)
    val notifySoon = strings().my.notifySoon
    val versionName = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull()
            .orEmpty()
    }

    // 캐시 크기는 디스크를 읽으므로 IO 에서 잰다.
    LaunchedEffect(viewModel) {
        viewModel.onAction(MyContract.Action.CacheSizeMeasured(context.imageCacheBytes()))
    }

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    is MyContract.Effect.NavigateToDetail ->
                        currentOnNavigateToDetail(effect.language, effect.contentId)

                    MyContract.Effect.NavigateToList -> currentOnNavigateToList()

                    is MyContract.Effect.OpenUrl -> context.openUrl(effect.url)

                    MyContract.Effect.ShowNotifySoon ->
                        Toast.makeText(context, notifySoon, Toast.LENGTH_SHORT).show()

                    MyContract.Effect.ClearImageCache -> {
                        withContext(Dispatchers.IO) {
                            val loader = SingletonImageLoader.get(context)
                            loader.diskCache?.clear()
                            loader.memoryCache?.clear()
                        }
                        viewModel.onAction(MyContract.Action.CacheSizeMeasured(context.imageCacheBytes()))
                    }
                }
            }
        }
    }

    MyScreen(
        onSearch = onSearch,
        uiState = uiState,
        versionName = versionName,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

private suspend fun android.content.Context.imageCacheBytes(): Long = withContext(Dispatchers.IO) {
    SingletonImageLoader.get(this@imageCacheBytes).diskCache?.size ?: 0L
}
