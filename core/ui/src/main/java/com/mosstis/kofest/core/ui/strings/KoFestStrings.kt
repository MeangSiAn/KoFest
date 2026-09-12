package com.mosstis.kofest.core.ui.strings

/**
 * 앱 문구.
 *
 * 언어를 시스템 로케일이 아니라 **앱 안에서** 바꾸므로 `res/values-en` 을 쓰지 않는다.
 * 원본은 `~/my/kofest/kofest/i18n/{ko,en}.json` 이고 이 파일은 그 값을 그대로 옮긴 것이다.
 *
 * 영어는 한국어의 번역이 아니라 **다시 쓴 것**이다. 문구를 추가할 때도 직역하지 않는다.
 */
data class KoFestStrings(
    val meta: Meta,
    val app: App,
    val nav: Nav,
    val action: Action,
    val home: Home,
    val list: ListStrings,
    val filter: Filter,
    val state: State,
    val intro: Intro,
    val calendar: Calendar,
    val my: My,
    val detail: Detail,
    val language: Language,
    val empty: Empty,
    val error: ErrorStrings,
    val loading: Loading,
    val place: PlaceStrings,
    val pick: PickStrings,
    val theme: ThemeStrings,
    val story: StoryStrings,
    val plan: PlanStrings,
    val area: AreaStrings,
    val region: Map<String, String>,
) {
    data class Meta(val name: String, val dateFormat: String)

    data class App(val name: String, val tagline: String)

    data class Nav(
        val home: String,
        val list: String,
        val calendar: String,
        val plan: String,
        val story: String,
        val saved: String,
    )

    /** 관광지. i18n 원본에 이미 있던 값 그대로다 */
    data class PlaceStrings(
        val title: String,
        val sub: String,
        val seeAll: String,
        val count: String,
        val filterType: String,
        val typeAll: String,
        val type12: String,
        val type14: String,
        val type28: String,
        val type38: String,
        val type39: String,
        val nearby: String,
        val noResult: String,
        val noOverview: String,
        val comingSoonRegion: String,
    ) {
        /** 유형 번호는 **언제나 한국어 번호**다 (영문 76 을 쓰지 않는다) */
        fun typeName(type: Int): String? = when (type) {
            12 -> type12
            14 -> type14
            28 -> type28
            38 -> type38
            39 -> type39
            else -> null
        }
    }

    /** i18n `theme.*`. 이름을 원본 키와 맞춘다 — 값을 옮길 때 대조가 쉽다 */
    data class ThemeStrings(
        val title: String,
        val sub: String,
        val count: String,
        val open: String,
        val close: String,
        val kindFestival: String,
        val kindPlace: String,
    )

    /** i18n `story.*` */
    data class StoryStrings(
        val title: String,
        val sub: String,
        val pinned: String,
        val empty: String,
        val more: String,
        val published: String,
        val updated: String,
    )

    /** i18n `plan.*`. `when` 은 Kotlin 예약어라 [whenLabel] */
    data class PlanStrings(
        val title: String,
        val lead: String,
        val where: String,
        val whenLabel: String,
        val howLong: String,
        val how: String,
        val build: String,
        val rebuild: String,
        val pickAreaFirst: String,
        val nights0: String,
        val nights: String,
        val moodFestival: String,
        val moodFestivalDesc: String,
        val moodEasy: String,
        val moodEasyDesc: String,
        val moodMany: String,
        val moodManyDesc: String,
        val resultTitle: String,
        val summary: String,
        val withFestivals: String,
        val day: String,
        val stay: String,
        val walk: String,
        val transit: String,
        val lunch: String,
        val dinner: String,
        val festivalTag: String,
        val noStops: String,
        val thin: String,
        val distanceNote: String,
        val save: String,
        val saved: String,
        /** 만드는 동안 돌아가며 보여주는 문구 6개. i18n `plan.loading1~6` */
        val loading: List<String>,
        val loadingDone: String,
    )

    /**
     * 광역권 8개 이름. i18n `area.*`. 홈의 지역 묶음과 자동 일정의 권역이 같이 쓴다.
     * (일정 서버는 아직 7개라 `busan` 을 보내면 400 — 적용방법.md)
     */
    data class AreaStrings(
        val seoul: String,
        val gyeonggi: String,
        val gangwon: String,
        val chungcheong: String,
        val jeolla: String,
        val gyeongsang: String,
        val busan: String,
        val jeju: String,
    )

    data class Action(
        val search: String,
        val back: String,
        val close: String,
        val retry: String,
        val seeAll: String,
        val share: String,
        val call: String,
        val save: String,
        val saved: String,
        val clearFilters: String,
    )

    data class Home(
        val ongoingTitle: String,
        val ongoingSub: String,
        /** "가볼 만한 여행지" — 관광지 8건. i18n `home.picks.*` */
        val picksTitle: String,
        val picksSub: String,
        val picksAll: String,
        val regionTitle: String,
        val regionSub: String,
        val regionAll: String,
        val bannerThisWeek: String,
        val bannerEndsSoon: String,
        val bannerFeatured: String,
    )

    data class ListStrings(
        val title: String,
        val titleRegion: String,
        val count: String,
        val monthCount: String,
        val tabFestival: String,
        val tabPlace: String,
    )

    data class Filter(
        val region: String,
        val regionAll: String,
        val period: String,
        val periodAll: String,
        val thisMonth: String,
        val nextMonth: String,
        val custom: String,
        val ongoing: String,
        val hasImage: String,
    )

    /** 오늘 뭐하지 (기획서 11). 문구는 웹 `/pick` 과 같다 */
    data class PickStrings(
        val title: String,
        val homeLead: String,
        val homeCta: String,
        val askLead: String,
        val askBody: String,
        val allow: String,
        val note: String,
        val byRegion: String,
        val denied: String,
        val locating: String,
        val locationFailed: String,
        val tapToDraw: String,
        val draw: String,
        val drawing: String,
        val again: String,
        val reset: String,
        val go: String,
        val count: String,
        val away: String,
        val none: String,
        val all: String,
        val fail: String,
    )

    data class State(
        val ongoing: String,
        val lastDay: String,
        val upcoming: String,
        val tomorrow: String,
        val today: String,
        val ended: String,
        val startsIn: String,
        val endsToday: String,
    )

    /**
     * 인트로는 **언어를 묻기 전에** 나오는 화면이라 ko/en 문구가 같다.
     * 한국어와 영어를 나란히 쓴다 — 외국인이 첫 화면에서 한글만 보면 잘못 받은 앱이라고 생각한다.
     */
    data class Intro(
        val splashHeadline: String,
        val splashSub: String,
        val dataSource: String,
        val langKicker: String,
        val langTitle: String,
        val langDesc: String,
        val langKoLabel: String,
        val langEnLabel: String,
        val langNote: String,
        val start: String,
        val changeAnytime: String,
    )

    data class Calendar(
        val title: String,
        val monthCount: String,
        val prevMonth: String,
        val nextMonth: String,
        val selected: String,
        val dayCount: String,
        val firstDay: String,
        val lastDay: String,
        val ongoing: String,
        val noneToday: String,
        val noneHint: String,
        val noneInLang: String,
        val today: String,
    )

    data class My(
        val title: String,
        /** i18n `my.trips` / `my.tripsEmpty` — 자동 일정 조건을 담아두는 곳 */
        val trips: String,
        val tripsEmpty: String,
        val savedCount: String,
        val upcomingCount: String,
        val savedList: String,
        val savedOrder: String,
        val pastToggle: String,
        val expand: String,
        val collapse: String,
        val unsave: String,
        val settings: String,
        val language: String,
        val notify: String,
        val notifyOn: String,
        val notifyOff: String,
        val notifySoon: String,
        val notifyAsk: String,
        val about: String,
        val dataSource: String,
        val dataSourceVal: String,
        val privacy: String,
        val feedback: String,
        val version: String,
        val localOnly: String,
        val emptyTitle: String,
        val emptyHint: String,
        val emptyCta: String,
        val terms: String,
        val tos: String,
        val license: String,
        val copyright: String,
        val clearCache: String,
        val clearCacheAsk: String,
        val versionLatest: String,
        val versionUpdate: String,
    )

    data class Detail(
        val overview: String,
        val info: String,
        val program: String,
        val hosts: String,
        val photos: String,
        val location: String,
        val nearby: String,
        val nearbyRange: String,
        val place: String,
        val playTime: String,
        val fee: String,
        val discount: String,
        val spendTime: String,
        val ageLimit: String,
        val booking: String,
        val placeInfo: String,
        val subEvent: String,
        val grade: String,
        val days: String,
        val oneDay: String,
        val more: String,
        val less: String,
        val directions: String,
        val openInMaps: String,
        val homepage: String,
        val photoCount: String,
        val noOverview: String,
        val noOverviewSoon: String,
    )

    data class Language(
        val current: String,
        val switch: String,
        val noticeTitle: String,
        val notice: String,
        val noticeWhy: String,
        val detailNotice: String,
        val moreHere: String,
    )

    data class Empty(
        val noResult: String,
        val noResultRegion: String,
        val noResultHint: String,
        val noSaved: String,
        val noSavedHint: String,
        val noPhotos: String,
        val searchNoResult: String,
    )

    data class ErrorStrings(
        val offline: String,
        val loadFailed: String,
        val loadFailedHint: String,
        val notFound: String,
    )

    data class Loading(val default: String)
}

/** `"축제 {count}"` 처럼 중괄호 자리표시자를 치환한다. i18n JSON 의 표기를 그대로 따른다. */
fun String.fill(vararg pairs: Pair<String, Any>): String =
    pairs.fold(this) { acc, (key, value) -> acc.replace("{$key}", value.toString()) }
