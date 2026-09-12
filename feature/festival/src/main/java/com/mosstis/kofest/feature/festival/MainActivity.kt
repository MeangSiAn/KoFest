package com.mosstis.kofest.feature.festival

import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.ui.base.BaseActivity
import com.mosstis.kofest.domain.festival.analytics.EventTracker
import com.mosstis.kofest.feature.festival.shell.AppViewModel
import com.mosstis.kofest.feature.festival.shell.KoFestApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val viewModel: AppViewModel by viewModels()

    @Inject
    lateinit var eventTracker: EventTracker

    /**
     * 쌓인 이벤트를 화면 밖으로 나갈 때 올린다.
     *
     * 임계치(20건)만 기다리면 한 번 쓰고 지우는 사용자의 기록이 영영 안 나간다.
     * 여행 중 잠깐 쓰는 앱이라 그런 세션이 오히려 많다.
     */
    override fun onStop() {
        super.onStop()
        eventTracker.flush()
    }

    /**
     * 선택한 언어가 테마의 서체 역할과 문구표를 함께 바꾸므로 Activity 에서 읽어 내려준다.
     *
     * NOTE(미확정): 기획서의 열어둔 질문 — 한국어로 바꿀 때 UI 까지 한글이 되는가,
     *  UI 는 영어를 유지하고 축제 목록만 바뀌는가. 지금은 **UI 와 데이터를 함께** 바꾼다.
     *  후자로 정해지면 언어 상태를 데이터용/UI용 둘로 나누면 된다.
     */
    @Composable
    override fun themeLanguage(): AppLanguage {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        return uiState.language
    }

    @Composable
    override fun ActivityContent() {
        KoFestApp()
    }
}
