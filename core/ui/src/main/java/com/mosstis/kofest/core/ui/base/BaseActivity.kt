package com.mosstis.kofest.core.ui.base

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.designsystem.theme.AppTheme
import com.mosstis.kofest.core.ui.strings.LocalStrings
import com.mosstis.kofest.core.ui.strings.stringsFor

/**
 * Compose 전용 Activity 기반.
 *
 * 공통 Theme / Window / lifecycle 훅만 제공한다. Repository 호출, 화면 상태,
 * Navigation 정책은 넣지 않는다. 이 위에 상속 계층을 더 만들지 않는다.
 */
abstract class BaseActivity : ComponentActivity() {

    // Hilt 가 @AndroidEntryPoint Activity 의 onCreate 를 override 해 SavedStateHandle 을 주입하므로
    // final 로 막지 않는다. 하위 Activity 는 onCreate 대신 아래 훅을 사용한다.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureWindow()
        onCreateBeforeContent(savedInstanceState)

        setContent {
            val language = themeLanguage()
            val strings = remember(language) { stringsFor(language) }

            AppTheme(language = language) {
                CompositionLocalProvider(LocalStrings provides strings) {
                    ActivityContent()
                }
            }
        }

        onCreateAfterContent(savedInstanceState)
    }

    protected open fun configureWindow() {
        enableEdgeToEdge()
    }

    protected open fun onCreateBeforeContent(
        savedInstanceState: Bundle?,
    ) = Unit

    /**
     * 화면에 적용할 언어. 서체 역할과 문구표가 이 값으로 갈린다.
     * 기본은 한국어이고, 사용자 선택을 반영해야 하는 Activity 만 override 한다.
     */
    @Composable
    protected open fun themeLanguage(): AppLanguage = AppLanguage.KO

    @Composable
    protected abstract fun ActivityContent()

    protected open fun onCreateAfterContent(
        savedInstanceState: Bundle?,
    ) = Unit
}
