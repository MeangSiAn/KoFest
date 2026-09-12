package com.mosstis.kofest.feature.festival.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import android.net.Uri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.core.designsystem.theme.KoFestColors
import com.mosstis.kofest.feature.festival.detail.DetailRoute
import com.mosstis.kofest.feature.festival.detail.DetailViewModel
import com.mosstis.kofest.feature.festival.home.HomeRoute
import com.mosstis.kofest.feature.festival.intro.IntroRoute
import com.mosstis.kofest.feature.festival.list.ListRoute
import com.mosstis.kofest.feature.festival.list.ListViewModel
import com.mosstis.kofest.feature.festival.magazine.MagazineRoute
import com.mosstis.kofest.feature.festival.magazine.StoryRoute
import com.mosstis.kofest.feature.festival.magazine.StoryViewModel
import com.mosstis.kofest.feature.festival.place.PlaceDetailRoute
import com.mosstis.kofest.feature.festival.place.PlaceDetailViewModel
import com.mosstis.kofest.feature.festival.search.SearchRoute
import com.mosstis.kofest.feature.festival.plan.PlanRoute
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
    val openSearch: () -> Unit = { navController.navigate(SEARCH_ROUTE) }
    val openDetail: (AppLanguage, Long) -> Unit = { language, contentId ->
        navController.navigate(detailRoute(language, contentId))
    }
    val openPlace: (AppLanguage, Long) -> Unit = { language, contentId ->
        navController.navigate(placeRoute(language, contentId))
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
                    onSearch = openSearch,
                    onNavigateToDetail = openDetail,
                    onNavigateToList = { regionCodes, ongoingOnly ->
                        navController.navigate(listRoute(regionCodes, ongoingOnly)) {
                            popUpTo(KoFestDestination.HOME.route)
                            launchSingleTop = true
                        }
                    },
                    onNavigateToPlace = openPlace,
                    onNavigateToStory = { language, slug ->
                        navController.navigate(storyRoute(language, slug))
                    },
                    onNavigateToMagazine = {
                        navController.navigate(KoFestDestination.MAGAZINE.route) {
                            popUpTo(KoFestDestination.HOME.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.statusBarsPadding(),
                )
            }

            composable(SEARCH_ROUTE) {
                SearchRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToFestival = openDetail,
                    onNavigateToPlace = openPlace,
                    onNavigateToFestivalList = { query ->
                        navController.navigate(listRoute(query = query)) { launchSingleTop = true }
                    },
                    modifier = Modifier.statusBarsPadding(),
                )
            }

            composable(
                route = "${KoFestDestination.LIST.route}?" +
                    "${ListViewModel.ARG_REGION}={${ListViewModel.ARG_REGION}}&" +
                    "${ListViewModel.ARG_ONGOING}={${ListViewModel.ARG_ONGOING}}&" +
                    "${ListViewModel.ARG_QUERY}={${ListViewModel.ARG_QUERY}}",
            ) {
                ListRoute(
                    onSearch = openSearch,
                    onNavigateToDetail = openDetail,
                    onNavigateToPlace = openPlace,
                    modifier = Modifier.statusBarsPadding(),
                )
            }

            composable(KoFestDestination.PLAN.route) {
                PlanRoute(
                    onSearch = openSearch,
                    onNavigateToDetail = openDetail,
                    onNavigateToPlace = openPlace,
                    modifier = Modifier.statusBarsPadding(),
                )
            }

            composable(KoFestDestination.MAGAZINE.route) {
                MagazineRoute(
                    onSearch = openSearch,
                    onNavigateToStory = { language, slug ->
                        navController.navigate(storyRoute(language, slug))
                    },
                    modifier = Modifier.statusBarsPadding(),
                )
            }

            // 글 본문은 탭 위에 덮인다. 안의 축제·관광지 카드는 앱 안의 상세로 이어진다.
            composable(
                route = "$STORY_ROUTE/{${StoryViewModel.ARG_LANG}}/{${StoryViewModel.ARG_SLUG}}",
            ) {
                // 탭이 없는 화면이라 아래 시스템 바 안쪽으로도 여백을 준다 — 없으면 본문 끝이 바 뒤로 들어간다.
                StoryRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToFestival = openDetail,
                    onNavigateToPlace = openPlace,
                    modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
                )
            }

            composable(KoFestDestination.MY.route) {
                MyRoute(
                    onSearch = openSearch,
                    onNavigateToDetail = openDetail,
                    onNavigateToList = {
                        navController.navigate(listRoute(regionCodes = emptyList(), ongoingOnly = false)) {
                            popUpTo(KoFestDestination.HOME.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.statusBarsPadding(),
                )
            }

            // 관광지 상세. 축제와 화면을 나눈 이유는 데이터가 다른 테이블이고
            // 날짜·프로그램·주최가 통째로 없기 때문이다 (기획서 03).
            composable(
                route = "$PLACE_ROUTE/{${PlaceDetailViewModel.ARG_LANG}}/{${PlaceDetailViewModel.ARG_CONTENT_ID}}",
            ) {
                PlaceDetailRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPlace = openPlace,
                    modifier = Modifier.statusBarsPadding().navigationBarsPadding(),
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
                        listRoute(regionCodes = emptyList(), ongoingOnly = false)
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
private const val STORY_ROUTE = "story"
private const val PLACE_ROUTE = "place"
private const val SEARCH_ROUTE = "search"

/** 광역권은 시도코드가 여럿이라 콤마로 잇는다 (경기·인천 = "41,28") */
private fun listRoute(
    regionCodes: List<String> = emptyList(),
    ongoingOnly: Boolean = false,
    query: String = "",
): String =
    "${KoFestDestination.LIST.route}?" +
        "${ListViewModel.ARG_REGION}=${regionCodes.joinToString(",")}&" +
        "${ListViewModel.ARG_ONGOING}=$ongoingOnly&" +
        "${ListViewModel.ARG_QUERY}=${Uri.encode(query)}"

/** 언어별로 contentId 가 다르므로 경로에 언어를 함께 담는다. */
private fun detailRoute(language: AppLanguage, contentId: Long): String =
    "$DETAIL_ROUTE/${language.code}/$contentId"

/** 관광지도 언어별로 contentId 가 다르다 */
private fun placeRoute(language: AppLanguage, contentId: Long): String =
    "$PLACE_ROUTE/${language.code}/$contentId"

/** 글도 언어별로 따로 쓴 것이라 slug 앞에 언어를 담는다 */
private fun storyRoute(language: AppLanguage, slug: String): String =
    "$STORY_ROUTE/${language.code}/$slug"
