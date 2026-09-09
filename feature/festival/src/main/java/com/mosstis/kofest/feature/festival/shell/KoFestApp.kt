package com.mosstis.kofest.feature.festival.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.feature.festival.calendar.CalendarRoute
import com.mosstis.kofest.feature.festival.detail.DetailRoute
import com.mosstis.kofest.feature.festival.detail.DetailViewModel
import com.mosstis.kofest.feature.festival.home.HomeRoute
import com.mosstis.kofest.feature.festival.intro.IntroRoute
import com.mosstis.kofest.feature.festival.list.ListRoute
import com.mosstis.kofest.feature.festival.list.ListViewModel
import com.mosstis.kofest.feature.festival.navigation.KoFestDestination
import com.mosstis.kofest.feature.festival.my.MyRoute

/**
 * 앱 셸 — 하단 탭 4개와 NavHost.
 *
 * 화면 이동은 여기서만 한다. ViewModel 은 NavController 를 알지 못하고
 * `UiEffect` 로 "어디로 가야 한다"만 알린다.
 *
 * 상세는 탭 위에 덮이는 화면이라 하단 탭을 숨긴다 (기획서 목업에 탭이 없다).
 */
@Composable
fun KoFestApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val current = KoFestDestination.fromRoute(route)
    // 언어별로 contentId 가 다르다. 테마 언어가 아니라 그 항목이 속한 데이터의 언어로 경로를 만든다 —
    // 언어를 바꾼 직후에는 화면에 아직 이전 언어의 데이터가 남아 있기 때문이다.
    val openDetail: (AppLanguage, Long) -> Unit = { language, contentId ->
        navController.navigate(detailRoute(language, contentId))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KoFestColors.Paper),
    ) {
        NavHost(
            navController = navController,
            startDestination = INTRO_ROUTE,
            modifier = Modifier.weight(1f),
        ) {
            // 스플래시는 매 실행, 언어 선택은 최초 1회. 판단은 IntroViewModel 이 한다.
            composable(INTRO_ROUTE) {
                IntroRoute(
                    onFinished = {
                        navController.navigate(KoFestDestination.HOME.route) {
                            // 인트로로 되돌아갈 수 없게 스택에서 지운다.
                            popUpTo(INTRO_ROUTE) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(KoFestDestination.HOME.route) {
                HomeRoute(
                    onNavigateToDetail = openDetail,
                    onNavigateToList = { regionCode, ongoingOnly ->
                        navController.navigate(listRoute(regionCode, ongoingOnly)) {
                            popUpTo(KoFestDestination.HOME.route)
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.statusBarsPadding(),
                )
            }

            composable(
                route = "${KoFestDestination.LIST.route}?" +
                    "${ListViewModel.ARG_REGION}={${ListViewModel.ARG_REGION}}&" +
                    "${ListViewModel.ARG_ONGOING}={${ListViewModel.ARG_ONGOING}}",
            ) {
                ListRoute(onNavigateToDetail = openDetail, modifier = Modifier.statusBarsPadding())
            }

            composable(KoFestDestination.CALENDAR.route) {
                CalendarRoute(onNavigateToDetail = openDetail, modifier = Modifier.statusBarsPadding())
            }

            composable(KoFestDestination.MY.route) {
                MyRoute(
                    onNavigateToDetail = openDetail,
                    onNavigateToList = {
                        navController.navigate(listRoute(regionCode = null, ongoingOnly = false)) {
                            popUpTo(KoFestDestination.HOME.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.statusBarsPadding(),
                )
            }

            // 히어로 이미지가 상태바 뒤까지 올라가므로 statusBarsPadding 을 주지 않는다.
            composable(
                route = "$DETAIL_ROUTE/{${DetailViewModel.ARG_LANG}}/{${DetailViewModel.ARG_CONTENT_ID}}",
            ) {
                DetailRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = openDetail,
                )
            }
        }

        if (current != null) {
            KoFestBottomBar(
                current = current,
                onSelect = { destination ->
                    val target = if (destination == KoFestDestination.LIST) {
                        listRoute(regionCode = null, ongoingOnly = false)
                    } else {
                        destination.route
                    }
                    navController.navigate(target) {
                        popUpTo(KoFestDestination.HOME.route) { saveState = true }
                        launchSingleTop = true
                        // 목록 탭은 항상 '전체 축제'다. 저장 상태를 되살리면 홈에서 지역·진행중으로
                        // 들어갔던 필터 목록이 칩까지 그대로 돌아온다 (실기기에서 확인).
                        // Navigation 은 저장 상태를 목적지 ID 로 찾고, 목록은 인자만 다른 같은 목적지라
                        // 구분할 수 없다. 그래서 목록만 되살리지 않고 매번 새로 연다.
                        restoreState = destination != KoFestDestination.LIST
                    }
                },
            )
        }
    }
}

private const val INTRO_ROUTE = "intro"
private const val DETAIL_ROUTE = "detail"

private fun listRoute(regionCode: String?, ongoingOnly: Boolean): String =
    "${KoFestDestination.LIST.route}?" +
        "${ListViewModel.ARG_REGION}=${regionCode.orEmpty()}&" +
        "${ListViewModel.ARG_ONGOING}=$ongoingOnly"

/** 언어별로 contentId 가 다르므로 경로에 언어를 함께 담는다. */
private fun detailRoute(language: AppLanguage, contentId: Long): String =
    "$DETAIL_ROUTE/${language.code}/$contentId"
