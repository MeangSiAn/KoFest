# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트

**KoFest** (Korea + Festival) — 한국 축제 정보 앱. 한국인/외국인 모두 대상, 한국어 + English.
이 저장소는 그 앱의 **Android 클라이언트**다. 데이터 수집기와 API 서버는 홈 NAS 쪽에 따로 있다.

### 개발 문서는 이 저장소 밖에 있다 — 반드시 먼저 읽는다

**기준은 `/Users/anmyeongseong/my/기획서/` 다** — `KoFest_기획서_v1.html`(화면, 섹션 00~11),
`kofest_개발문서3.md` + `적용방법.md`(서버와 주고받는 것, 최신), `kofest-i18n/{ko,en}.json`(문구 263키).
아래 `~/my/kofest/kofest/` 는 **옛 기획서**이고 데이터·인프라 설명만 아직 유효하다. 둘이 다르면 **v1 을 따른다.**

**화면 구성은 기획서를 그대로 따른다.** 기획서가 뺀 섹션을 "데이터가 아직 없어 빈자리가 보인다"는 이유로
폴백으로 되살리거나, 기획서에 없는 숨김·조건을 넣지 않는다 — 비면 왜 비었는지 쓴다. 기획서에 정의가 없는
상황은 바꾸지 말고 그대로 둔 채 결과를 보고한다. (2026-09-10 "이번 주말" 폴백으로 지적받은 규칙)

작업 시작 전 `/Users/anmyeongseong/my/kofest/kofest/` 도 읽는다. 이 저장소보다 훨씬 많은 결정이 거기 담겨 있다.

| 경로 | 내용 |
|---|---|
| `README.md` | 진행 상황, 다음 작업 |
| `docs/01_인프라.md` | NAS 도커 구성, 컨테이너/포트 |
| `docs/02_데이터.md` | DB 스키마, TourAPI 필드 매핑, **데이터 함정** |
| `docs/03_기획_홈목록.md` | 홈/목록 화면 정의, 디자인 토큰 |
| `docs/04_작업원칙.md` | 이 프로젝트의 개발 방식 (아래 요약) |
| `docs/05_남은일.md` | API 엔드포인트 명세, 열어둔 질문 |
| `KoFest_기획서.html` | 시각적 기획서 (브라우저로 열 것) |
| `i18n/ko.json`, `i18n/en.json` | 앱 문구 원본 |
| `collector/*.py` | NAS 에서 실제 도는 수집기 코드 (스키마의 실제 정의는 `db.py` 의 `SCHEMA`) |

## 현재 상태

기획서의 **홈 / 목록 화면이 UI 까지 구현**되어 있다. API 서버(`festival-api`)가 아직 없어
데이터는 `:data:festival` 의 **`FakeFestivalRepository`** 가 대신 내려준다.

```
:app                  Composition Root (KoFestApplication, Launcher Manifest) — Compose 없음
:core:common          AppLanguage. 순수 Kotlin/JVM, Android·Compose 반입 금지
:core:presentation    BaseViewModel + UiState/UiAction/UiEffect 계약. Compose 없음
:core:designsystem    KoFestColors / KoFestDimens / KoFestTypography / AppTheme + 공용 컴포넌트
:core:ui              BaseActivity, 문구표(KoFestStrings + Ko/En + LocalStrings)
:domain:festival      Festival / HomeFeed / FestivalFilter / FestivalRepository / UseCase
:data:festival        FakeFestivalRepository + LanguagePreferenceStore + Hilt binding
:feature:festival     MainActivity · 앱 셸(하단탭+NavHost) · intro / home / list(+달력·관광) / detail / place / magazine / plan / my
```

레이어 규칙: `app → feature + data`, `feature → core + domain`, `data → domain`,
`core:ui → core:designsystem + core:common`, `core:presentation → core:common`.
`core → feature`, `feature A → feature B`, `feature → data` 는 금지.

**화면들(홈·목록·달력·상세·MY)은 모두 축제 도메인을 공유하므로 feature 모듈 하나(`:feature:festival`)에 둔다.**
Activity 하나마다 feature 모듈을 만들지 않는다.

### 구현된 것 / 안 된 것

| | 상태 |
|---|---|
| 디자인 토큰(색·치수·타이포) | 완료 (`:core:designsystem`) |
| 문구 ko/en 이관 + 인앱 언어 전환 | 완료 (`:core:ui/strings`) |
| 홈 (배너·진행중·주말·지역·언어안내) | 완료 |
| 목록 (필터바·월 헤더 sticky·행·커서 페이징) | 완료 |
| 상태 화면 (로딩 뼈대·빈 결과·오프라인 띠) | 완료 |
| 상세 (히어로 페이저·액션·소개·안내·프로그램·주최·사진·위치·주변) | 완료 — `feature/festival/detail/` |
| 저장 (하트 + 저장 탭) | 완료 — DataStore 로컬 저장, `data/festival/local/` |
| 달력 (월 격자·점 밀도·날짜 선택·월 이동/스와이프·빈 날 안내) | 완료 — **목록 화면 안의 `목록 \| 달력` 전환** (`feature/festival/list/CalendarView.kt`) |
| 통계 `POST /events` | 완료 — `domain/festival/analytics/` + `data/festival/analytics/QueuedEventTracker` |
| 앱 설정 (`minAppVersion` · `ads`) | 부분 — `AppConfig` 로 받아 MY 의 버전 줄에 '최신/업데이트 있음'. 광고 SDK 는 미도입 |
| MY (저장 목록·지난 축제 접기·스와이프 해제·설정·약관·앱 정보) | 완료 — `feature/festival/my/`, 저장 탭을 대체 |
| 홈 서버 배너 | 완료 — `/home.banners`(관리자 등록) 앞, 부족분은 `BannerRule` 로 채움 |
| 매거진 (목록 · 블록 본문) | 완료 — `feature/festival/magazine/`, `GET /api/stories` (실기기 확인) |
| 테마 캐러셀 + 그 자리 펼침 | 완료 — `home/HomeDiscoverSections.kt`, `/home.themes` (별도 엔드포인트 없음) |
| 관광지 (목록 관광 탭 · 상세 · 홈 "가볼 만한 여행지") | 완료 — `feature/festival/place/`, `GET /api/places`. **유형 필터 시트는 미구현** (`Action.SelectPlaceType` 만 있다) |
| 자동 일정 짜기 (입력 · 만드는 중 · 결과 · 다시 짜기) | 완료 — `feature/festival/plan/`, `GET /api/plan`. **MY "내 일정" 저장은 미구현** (i18n `my.trips`, `plan.save` 있음) |
| 홈 "이번 주말" | **없앴다** — 기획서 v1 이 뺐다. 서버는 `/home.weekend` 를 계속 주지만 그리지 않는다 |
| 홈 지역 8광역권 묶기 | 완료 — `RegionGroup` + `groupByRegion()` |
| 검색 | **미구현** — 화면 정의가 없다 |
| 목록 축제/관광 세그먼트 · 홈 관광지 섹션 · 관광지 상세 | **미구현** — `GET /places` 가 서버에 없다 (기획서도 [미확정]) |
| 실제 API 연동 | 완료 — Retrofit + kotlinx.serialization, `RemoteFestivalRepository` |
| 인트로 (스플래시 → 최초 1회 언어 선택) | 완료 — `feature/festival/intro/`, 건수는 `/health` 에서 실시간 |
| 언어 선택 영구 저장 | 완료 — `data/festival/local/LanguagePreferenceStore` (DataStore) |
| 서체 3종 실제 폰트 | **미적용** — `KoFestFonts` 가 시스템 폴백을 쓴다 (아래 참조) |

### API

Base URL 과 키는 **`local.properties`** 에 있고(`kofest.baseUrl`, `kofest.apiKey`) `:data:festival` 의
`BuildConfig` 로 들어간다. 이 파일은 `.gitignore` 되어 있다 — **키를 소스에 적지 않는다.**
키가 비어 있으면 요청에 헤더가 붙지 않아 401 이 나고, 화면은 "불러오지 못했습니다 + 다시 시도" 를 띄운다.

실제 응답에서 확인한 것 (`data/festival/remote/FestivalDto.kt` 주석에 같은 내용이 있다):

| | |
|---|---|
| 목록 | `{ items[], counts{ko,en}, nextCursor }` — **전체 건수(totalCount)를 주지 않는다** |
| 홈 | `{ ongoing[], weekend{from,to,items[]}, regions[], counts }` — **banners 를 주지 않는다** |
| 정렬 | `start_date, content_id` 오름차순 |
| 기본 필터 | 종료된 축제는 오지 않는다. 서버가 오늘 기준으로 거른다 (앱이 `from` 을 보낼 필요 없다) |
| `region` | 서버가 이름으로 완성해서 준다. `lang=en` 이면 "Seoul" |
| `state` | 서버가 정한다. 앱은 오늘 날짜와 비교해 다시 계산하지 않는다 |

- **배너는 `/home.banners` 의 관리자 배너가 앞에 온다** (`imageUrl` 은 `/media/…` 상대경로 → data 가 절대경로로, `linkValue` 는
  스킴 없이 올 수 있어 `https://` 를 붙인다). 4장에 못 미치는 만큼만 `BannerRule` 이 진행중 축제로 채운다 (`Banner` 는 sealed)
- **목록/홈 응답에는 원본 이미지(`imageUrl`)가 없고 300×200 썸네일만 온다.** 배너·카드에는 너무 작아서
  `data/festival/mapper/ImageUrls.kt` 가 썸네일 URL 의 `_image3_` 를 `_image2_` 로 바꿔 원본을 유도한다
  (20/20 존재 확인, 상세 API 값과 5/5 일치). 화면(`FestivalImage`)은 실패하면 썸네일로 되돌아간다.
  서버가 `imageUrl` 을 내려주기 시작하면 유도는 자동으로 꺼진다. 목록 행·갤러리 썸네일은 기획서대로 썸네일을 쓴다
- **알려진 서버 이슈** — `/health` 의 `regions.source` 가 `"fallback"`, `loaded: 0` 이다.
  master-data 의 `common.legal_dong` 을 못 읽고 있어서 `region` 이 **시도까지만** 온다
  ("경기도 이천시" 가 아니라 "경기도"). 앱은 서버가 준 값을 그대로 보여주므로 앱 쪽 수정은 필요 없다
- 실패는 `FestivalDataException`(Unauthorized / Network / Server)로 타입을 나눠 던진다.
  응답 본문의 `message`·`hint` 를 그대로 담는다 — 서버 로그를 못 보는 상황이 훨씬 많다

### 상세 (`GET /festivals/{lang}/{contentId}`)

실제 응답 25건(ko)+25건(en)을 보고 만들었다. 데이터가 얇다는 것이 전제다:

- `info` 는 **빈 값의 키 자체가 오지 않는다** → `Map<String,String>` 으로 받고 온 키만 그린다 (`InfoKey` 순서 = 기획서 표 순서)
- 영어는 `info`·`hosts` 가 25/25 비어 있고 `overview`·`imageUrl` 도 자주 null → 기획서의 "비어 있을 때" 규칙이 곧 기본 상태다
- **비어 있는 섹션은 제목까지 통째로 숨긴다.** "프로그램 — 정보 없음" 을 남기지 않는다
- **눌러도 아무 일 없는 버튼을 두지 않는다.** 전화·홈페이지·길찾기는 값이 있을 때만 만든다. 회색 처리 금지
- `homepage` 는 라벨 섞인 자유 텍스트(`"공식 홈페이지 https://…\n공식 인스타그램 https://…"`) → 첫 URL 만 뽑는다
- `hosts` 에 같은 기관이 두 번 오는 일이 잦다 → 이름으로 합치고 전화 있는 쪽을 남긴다
- `detailStatus` 는 `ok/partial/none`. 현재 서버는 전부 `ok` 지만 `none` 이면 "준비 중" 안내를 낸다
- 지도는 앱에 넣지 않는다. `geo:` 인텐트로 외부 지도앱에 넘긴다 (API 비용 0)

### 언어별 contentId 와 화면 이동

**같은 축제라도 ko/en 의 contentId 가 다르다.** 상세 경로는 `detail/{lang}/{contentId}` 이고, 언어는
**테마 언어가 아니라 그 항목이 속한 데이터의 언어**를 Effect 에 실어 보낸다 (`HomeContract.State.feedLanguage`,
`ListContract.State.sectionsLanguage`, `SavedFestival.language`). 언어를 바꾼 직후에는 화면에 아직 이전 언어의
데이터가 남아 있기 때문이다(목록을 비우지 않는 설계). 테마 언어로 경로를 만들면 404 가 난다 — 실제로 났다.

Route 안의 `LaunchedEffect` 에서 상위 콜백을 부를 때는 `rememberUpdatedState` 로 읽는다. 키에 없는 람다는 첫 값으로 굳는다.

### 하단 탭 (`shell/KoFestBottomBar.kt`, `shell/KoFestApp.kt`)

- **탭은 바 높이(60dp)를 `fillMaxHeight()` 로 꽉 채운다.** Row 에 상하 패딩을 주고 탭을 `weight(1f)` 만 두면
  세로는 내용(37dp)만 눌리고 패딩 23dp 가 죽은 띠가 된다 — "가끔 탭이 안 눌린다" 로 나타났던 실제 버그.
  `RowScope.weight` 는 가로만 분배한다
- 탭 이동은 `popUpTo(HOME) { saveState = true } + launchSingleTop + restoreState` 인데 **목록 탭만 `restoreState = false`** 다.
  Navigation 은 저장 상태를 목적지 ID 로 찾고 목록은 인자(`region`, `ongoing`)만 다른 같은 목적지라,
  홈에서 지역/진행중으로 들어갔던 필터 목록이 탭을 눌러도 칩까지 그대로 되살아났다 (실기기 확인). 목록 탭 = 항상 전체 축제
- `restoreState` 가 홈 탭을 삼키지는 않는다 — 저장 키는 **팝된** 목적지이고 홈은 `popUpTo` 대상이라 팝되지 않는다
  (`navigation-runtime 2.9.8` `NavControllerImpl.kt:512,532,1197` 로 확인)
- 인트로가 `startDestination` 이고 홈 진입 시 `popUpTo(intro) { inclusive = true }` 로 지운다. 따라서
  `navController.graph.findStartDestination()` 을 popUpTo 대상으로 쓰면 안 된다 (스택에 없다). 항상 `HOME.route` 를 쓴다

### 달력 (`feature/festival/list/CalendarView.kt`) — 탭이 아니라 목록 안의 전환

- **탭이 아니다.** 기획서 05 가 목록 화면 상단의 `목록 | 달력` 세그먼트로 옮겼다. 하단 탭은 홈 · 목록 · MY 셋이다
- 두 보기가 **같은 필터를 공유**하므로 상태도 `ListContract.State.calendar` 하나에 있고 ViewModel 도 `ListViewModel` 하나다.
  지역을 부산으로 걸어둔 채 달력으로 바꾸면 부산 축제만 찍힌다
- 달력은 목록의 필터를 **그대로** 쓴다 (기간만 보고 있는 달이 정한다). '진행중'은 서버가 오늘 기준으로 판단하므로
  이 칩을 켜고 다른 달을 열면 거의 빈다 — 기획서에 정의가 없어 **감추지 않고 그대로 둔다.** 결정이 나면 그때 바꾼다
- 달력 데이터는 **세그먼트를 눌렀을 때** 받는다. 목록만 보는 사람에게 매달 세 번씩 부르지 않는다

- **월 단위로 한 번만** 부른다: `GetMonthFestivalsUseCase` 가 `from/to` 로 커서가 끝날 때까지 받는다 (상설 행사 때문에 한 달이 50건을 넘는다).
  `from/to` 를 주면 서버가 종료된 축제도 준다 → 지난 달·작년도 볼 수 있다. 이전·다음 달은 미리 받아둔다
- 기간이 있는 축제는 **걸치는 날 전부**에 점을 찍는다 (`spreadByDay`). 시작일만 찍으면 16일에 가는 사람이 못 찾는다
- 점은 밀도만 말한다: 1~3개 그대로, 4건 이상은 3개 고정, 황토 = 오늘 기준 진행중. 숫자는 날짜를 눌러서 본다
- 날짜를 눌러도 화면을 바꾸지 않는다. 아래 목록만 바뀌고, 행의 날짜 열은 **선택한 날이 아니라 축제 시작일**이다
  (`FestivalRow(whenText = "10.15 – 10.18 · 첫날/진행중/마지막 날")`)
- 월을 넘기면 선택은 해제되고 목록은 그 달 전체. 영어에서는 빈 날에 "한국어로 보면 N건" 을 붙이기 위해 KO 달도 함께 받는다
- 격자는 **월요일 시작** (기획서 M T W T F S S)

### 상세 — '위치' 섹션은 없다 (2026-09-10 사용자 결정)

- 기획서 04 에는 "위치 → 지도 앱에서 열기" 상자가 있지만, 액션 줄의 **길찾기와 같은 지도앱을 열어 겹친다**고 판단해 뺐다.
  축제 상세·관광지 상세 모두 길찾기 버튼만 남는다. `DetailMapBox` 는 삭제했다

### 자동 일정 (`feature/festival/plan/`)

- 입력 → 만드는 중(최소 2초, i18n `plan.loading1~6` 순환) → 결과가 한 화면 안에서 바뀐다. 칩을 눌러도 다시 만들지 않고 **결과 보기**를 눌러야 부른다
- '언제'는 기획서 09 의 상자 둘(`.pl-dbox`)이다. 왼쪽(날짜)을 누르면 Material3 `DatePickerDialog` — 오늘~6개월만 고를 수 있고,
  확인 버튼 문구가 i18n 에 없어 날짜를 누르면 바로 반영·닫힘. 오른쪽(박수)은 `DropdownMenu` 로 당일~3박 4일
- **`launch` 안에서 `async` 를 쓰지 않는다.** 실패한 `async` 의 예외는 부모 Job 으로 올라가 `launchCatching` 의 try/catch 를 지나쳐 **앱이 죽는다** (2026-09-10 실기기에서 겪음).
  요청을 순차로 받고 남은 시간만 `delay` 한다
- 결과는 저장하지 않는다. 같은 입력이면 같은 결과라 조건만 들고 있으면 된다 — MY "내 일정" 은 아직 없다

### 통계 (`POST /events`)

- 이벤트 이름은 **서버 계약**이다 (`domain/festival/analytics/AppEvent`). 문자열을 마음대로 바꾸지 않는다
- 한 건씩 보내지 않는다. `QueuedEventTracker` 가 DataStore 큐에 쌓아 **20건이 되면** 보내고,
  `MainActivity.onStop` 에서도 보낸다 — 임계치만 기다리면 한 번 쓰고 지우는 사용자의 기록이 영영 안 나간다
- 전송 실패는 **큐를 비우지 않는다.** 다음 이벤트에 얹혀 다시 나간다. 실패는 로그로 남긴다 (조용히 삼키지 않는다)
- `deviceId` 는 앱이 만든 UUID 다. **광고 식별자(GAID)를 쓰지 않는다** — 개인정보 처리방침에 그렇게 적혀 있다
- 계측 지점: 화면은 각 ViewModel `init`, 언어 전환은 `SetLanguageUseCase`, 저장/해제는 `ToggleSavedFestivalUseCase`.
  **UseCase 에 붙인 것은 진입점이 여럿이기 때문이다** (언어 전환은 헤더·MY·빈 화면 버튼 세 곳)
- 배너 노출은 스크롤이 **멈춘 뒤**의 장만 센다. 미는 도중 스쳐 간 장까지 세면 노출 수가 부풀려진다
- 요청 형식은 **실제로 보내 확인했다** (2026-09-10): 문서 그대로의 body 로 `200 {"accepted": 2}`. `contentId` 가 없는 이벤트도 받는다.
  `accepted` 가 보낸 수와 다르면 경고 로그를 남긴다
- `search` 는 검색 화면이 없어 아직 쏘는 곳이 없다

### MY (`feature/festival/my/`) — 저장 탭을 대체

- 저장 목록은 **시작일 가까운 순**(저장 순 아님). 끝난 축제는 지우지 않고 "지난 축제 N" 아래로 접는다. 끝났는지는 저장 시점 `state` 가 아니라
  `endDate < today` 로 본다 (저장 스냅샷은 오래됐을 수 있다). 왼쪽 스와이프 = 저장 해제
- 기기 쪽 일(캐시 크기·비우기 `SingletonImageLoader.diskCache`, 버전, 토스트, 외부 URL)은 `MyRoute` 에서만 한다
- 약관·정책 URL 은 `common/KoFestLinks.kt` 한 곳. **2026-09-08 기준 서버에 아직 없다** (404, 키 없이는 401)
- "의견 보내기" 행은 **받을 이메일이 정해지지 않아 넣지 않았다.** 눌러도 아무 일 없는 행보다 없는 행이 낫다. 주소가 정해지면 `mailto:` 로 추가
- 알림은 자리만 있다 — 누르면 "준비 중입니다". 권한은 첫 저장 직후에 묻는 것이 기획서 방향

### 서버 — 앱용 API 는 `/api/` 아래 (개발문서3 · 적용방법.md, 2026-09-10 실제 응답 확인)

```
/festivals  /festivals/{lang}/{id}  /home  /health  /events   기존 그대로
/api/places  /api/places/{lang}/{id}  /api/stories  /api/stories/{lang}/{slug}  /api/plan   앱용 JSON
/  /browse  /places  /plan  /story                             ← 웹 페이지. 앱이 부르지 않는다
```

- `/places` `/plan` 은 이미 웹 화면 주소라 같은 경로에 API 를 둘 수 없었다. **접두사를 빼면 HTML 이 온다**
- **테마는 `/home.themes` 뿐이다.** `/themes` 엔드포인트는 없다 (앱에 테마 화면이 없어 따로 부를 이유가 없다)
- 스웨거: `https://kofest.mosstis.com/docs`. **문서에 있다고 붙이지 말고 여기서 확인한다**
- 실제 응답이 문서와 다른 곳: `/api/plan` 에 `title` 이 없고 `areaName`·`moodName`·`days[].count` 가 온다 (제목은 앱이 `resultTitle` 로 만든다).
  `/api/places` 커서는 `"2"` 같은 페이지 번호다. `/festivals` 에 `total` 이 없다. `ads` 는 `{enabled, listFirstIndex, listInterval, onDetail}`
- 404 나 HTML 이 오면 `apiCall` 이 `FestivalDataException.NotReady` 로 바꾼다 — 서버가 옛 버전으로 돌아가도 앱이 죽지 않는다
- **`notice` 의 형태를 아직 모른다.** 응답이 계속 `null`. 확인 전까지 파싱하지 않는다
- **자동 일정 권역은 서버가 아직 7개다.** i18n 은 8개(`area.busan`)인데 `area=busan` 을 보내면 400 (적용방법.md). 앱의 `TravelPlanRequest.Area` 도 7개로 뒀다

### 지역: 서버는 시도, 화면은 광역권

- `/home` 의 `regions` 는 **시도 16개**를 준다. 그중 `12` 는 **"전남광주통합특별시"** — 실재하는 행정코드가 아니라
  수집 쪽 합성 값이다. 그대로 화면에 내보내면 안 된다 (기획서 09 의 전라 권역에 `12` 가 있는 이유가 이것)
- 그래서 앱이 `RegionGroup` 으로 **8개 광역권**(서울 / 경기·인천 / 강원 / 충청 / 전라 / 경상 / 제주 / 부산·울산)으로 묶는다.
  이름은 i18n `area.*`. **자동 일정의 권역은 7개**로 다르다 — 거기서는 부산·울산이 경상에 들어간다 (기획서 09, 서버도 7개)
- **서버는 지역을 하나만 받는다.** `region=41,28` 은 0건, `region=41&region=28` 은 뒤엣것만 먹는다.
  그래서 광역권을 누르면 `RemoteFestivalRepository.getMergedFestivals` 가 시도별로 부른 뒤 합친다.
  커서가 `"2026-10-15|293084"` = `(start_date, content_id)` 이고 정렬이 모든 지역에서 같아서
  **합친 결과의 마지막 항목 하나로 모든 시도의 다음 커서를 만들 수 있다** — 시도별 커서를 따로 들고 다니지 않는다

### 인트로 (`feature/festival/intro/`)

- 스플래시는 매 실행, 언어 선택은 `hasSelectedLanguage == false` 일 때 1회. 두 단계는 한 Composable 의 `Phase` 라 배경이 깜빡이지 않는다
- 스플래시 최소 800ms · 최대 3초. 'API 첫 응답'은 `/health` 건수 조회다. 실패해도 막지 않고 **숫자 없이** 언어 이름만 보여준다 ("0건" 금지)
- 기본 선택은 기기 언어가 한국어면 KO, 그 외 전부 EN. 저장은 사용자가 '시작하기'를 눌렀을 때만 한다
- 인트로 문구는 ko/en 이 **같다** (언어를 묻기 전이라 둘 다 쓴다). 서체 역할도 뒤집지 않는다
- "3초 넘기면 캐시된 데이터로 홈" 은 아직 캐시 계층이 없어 홈이 자체 오류 화면을 띄운다. 캐시가 생기면 여기서 잇는다

### 새 기능을 추가할 때

다른 도메인을 쓰는 업무 feature 는 `:domain:<feature>` / `:data:<feature>` / `:feature:<feature>`
세 모듈을 함께 만든다. Repository 구현체 조립은 `:app` 의 Hilt 그래프가 담당한다.

## 빌드 / 테스트

```bash
./gradlew :app:assembleDebug            # 디버그 빌드
./gradlew :app:installDebug             # 연결된 기기/에뮬에 설치
./gradlew testDebugUnitTest             # 전 모듈 유닛 테스트
./gradlew :app:testDebugUnitTest --tests "com.mosstis.kofest.ExampleUnitTest"   # 단일 테스트
./gradlew :app:testDebugUnitTest --tests "*ExampleUnitTest.addition_isCorrect"  # 단일 메서드
./gradlew :app:connectedDebugAndroidTest  # 계측 테스트 (기기 필요)
./gradlew :app:lintDebug                # Android Lint
./gradlew projects                      # 모듈 목록 확인
```

- Gradle 9.4.1 / AGP 9.2.1 / Kotlin 2.2.10, JDK toolchain 21, 모든 모듈 Java 17, `compileSdk 36.1` · `minSdk 24` · `targetSdk 36`
- **configuration cache 가 켜져 있다** (`org.gradle.configuration-cache=true`). 빌드 스크립트에 config-cache 비호환 코드를 넣지 않는다
- **AGP 9 built-in Kotlin 을 쓴다.** `org.jetbrains.kotlin.android` 플러그인을 중복 적용하지 않는다
- **Annotation processing 은 KSP 만 쓴다** (`ksp(...)`). `kapt` 는 쓰지 않는다. KSP 는 built-in Kotlin 과 호환되는 2.3.x 계열(`2.3.11`)이어야 한다 — Kotlin 결합 계열(`2.2.10-2.0.2`)은 `kotlin.sourceSets DSL is not allowed with built-in Kotlin` 으로 실패한다
- **DI 는 Hilt + KSP.** `@HiltAndroidApp`(`:app`) / `@AndroidEntryPoint`(Activity) / `@HiltViewModel`(ViewModel)
- **`local.properties` 를 `providers.fileContents` 로 읽는다.** `File.readText()` 로 읽으면 값이 바뀌어도 configuration cache 가 안 깨진다
- ktlint / detekt 는 아직 없다. 도입하려면 먼저 확인받는다
- 버전은 전부 `gradle/libs.versions.toml` 버전 카탈로그로 관리한다. 의존성을 `build.gradle.kts` 에 직접 문자열로 적지 않는다
- **`androidx.hilt` 는 1.3.0 에 고정**되어 있다. 1.4.0 은 `compileSdk 37` 을 요구해 현재 설정으로 빌드가 깨진다
- **Kotlin 2.2.10 이 의존성 상한이다.** Kotlin 2.4 로 빌드된 라이브러리는 메타데이터를 못 읽어 컴파일이 깨진다 (그래서 Coil 은 최신 3.6.x 가 아니라 **3.4.0**). 새 라이브러리를 넣기 전에 그 pom 의 `kotlin-stdlib` 버전을 확인한다
- **core library desugaring 이 켜져 있다.** `minSdk 24` 에서 `java.time` 을 쓰기 위한 것이다. `java.time` 을 쓰는 새 Android 모듈에는 `isCoreLibraryDesugaringEnabled = true` 와 `coreLibraryDesugaring(libs.desugar.jdk.libs)` 를 함께 넣는다
- Compose 는 `:core:designsystem` / `:core:ui` / `:feature:*` 에만 적용한다. `:app` / `:core:common` / `:core:presentation` 에는 넣지 않는다
- release 빌드는 현재 `optimization { enable = false }` — R8 미적용 상태다. 켤 때 keep rule 은 `app/src/main/keepRules/` 에 넣는다 (AGP 가 이 폴더의 파일들을 합쳐 R8 에 넘긴다)

## 전체 아키텍처

```
공공데이터포털 TourAPI
  KorService2 / EngService2  ·  /searchFestival2  ·  오퍼레이션당 하루 1,000회
        │  매일 04:00 배치 (festival-scheduler, Python)
        ▼
PostgreSQL  festival-db  (NAS, postgres:16-alpine, 외부 5435)
  festival (PK: lang, content_id) · sync_log
        │  ← 지역명은 별도 DB(master-data.common.legal_dong)에서 부팅 시 메모리 적재
        ▼
FastAPI  festival-api  **동작 중** — https://kofest.mosstis.com
  GET /festivals · /festivals/{lang}/{contentId} · /home · /health · /diagnose/region/{code}
  인증: X-API-Key 헤더 (실패 시 401 + {error, message, hint})
        ▼
Android 앱 (이 저장소)
```

**앱은 계산하지 않고 서버가 완성해서 준다**는 것이 이 설계의 핵심 규칙이다.

- `region` — 서버가 지역코드를 이름으로 변환해서 내려준다. 앱이 코드→이름 매핑을 갖지 않는다
- `state` (`ongoing`/`upcoming`/`ended`) — **서버가 정한다**. 기기 시간이 틀어져 있으면 표시가 어긋나므로 앱에서 날짜 비교로 계산하지 않는다
- `counts: {ko, en}` — 매 응답에 포함. 언어별 건수 안내 문구에 실제 숫자를 노출하기 위한 것
- 홈은 `GET /home` **한 번**으로 채운다. 섹션별로 나눠 호출하지 않는다
- 목록은 **커서 페이징** (`cursor: "2026-10-15|293084"`). offset 은 04:00 배치로 데이터가 갱신되면 어긋난다

## 데이터에서 반드시 알아야 할 것

`docs/02_데이터.md` 가 원본. 앱 작업에 직접 영향을 주는 것만:

- **언어별 contentid 가 다르다.** 같은 축제가 ko 293084 / en 293085. PK 는 `(lang, content_id)` 이고 언어는 독립 저장이다. 앱은 **자기 언어 데이터만 조회**하고, 언어 간 매칭을 시도하지 않는다
- **정렬은 예외 없이 `ORDER BY start_date, content_id`.** 동점 기준이 없으면 페이징에서 누락·중복이 생긴다
- 영어 데이터는 903 대 235 로 **26% 수준**이다. 영어 화면에서 목록이 비거나 짧은 것은 버그가 아니라 데이터 현실이다. 빈 화면에는 반드시 "왜 비었는지"를 쓴다
- 사진 없는 축제가 상당수다. 회색 네모를 쓰지 않고 자주색 대각선 결 + 한자 한 글자 면을 쓴다 (CSS 정의는 `docs/03_기획_홈목록.md`)
- 이미지 URL 에 `http://` 가 섞여 있을 수 있다 (**[미확인]**). Coil 등에 붙일 때 cleartext 정책을 확인한다
- 좌표는 `mapy → lat`, `mapx → lng`. **순서를 뒤집기 쉽다**
- **몇 해 전에 시작해 지금도 진행중인 상설 행사가 섞여 있다** (예: 2022-11-01 시작). 시작일 오름차순이라 목록 맨 앞을 이런 항목이 채우고, 월 헤더에 연도가 없으면 "November 다음 January" 로 읽힌다 — 그래서 다른 해의 월에는 연도를 함께 쓴다
- **목록 탭 제목은 '둘러보기'** (i18n `list.title`). 기획서는 축제/관광 세그먼트를 두지만 관광 API 가 없어 세그먼트는 아직 없다
- 사진이 있는 축제 비율이 높고 **대부분 흰 바탕 포스터**다. 이미지 위 배지는 불투명도를 충분히 줘야 흰 글씨가 읽힌다 (기획서 목업의 0.55 로는 안 보인다)

## 디자인 토큰

`docs/03_기획_홈목록.md` 가 원본이고, 값은 `:core:designsystem` 의 `theme/` 에 이미 옮겨져 있다.

- 색: `KoFestColors` (Jaju `#6B1E32` / JajuDeep / Hwangto / Paper / Ink / Muted / Line + 파생 토큰)
- 치수: `KoFestDimens` (`ScreenMargin = 22.dp` 는 모든 요소가 쓰는 하나의 좌우 여백, `Peek = 30.dp`)
- 타이포: `KoFestTypography` — 기획서 목업의 px 값을 스타일 이름별로 옮긴 것. `KoFestTheme.type.rowTitle` 처럼 쓴다
- **색과 치수를 화면 코드에 직접 쓰지 않는다.** 기획서에 없는 값이 필요하면 먼저 토큰으로 정의한다
- Dynamic Color(Material You)는 **꺼져 있다.** 기기 배경색이 자주를 덮으면 이 앱의 인상이 사라진다. 다크 팔레트도 아직 없다 (기획서가 종이색 단일 팔레트 전제)
- **서체 역할은 언어에 따라 뒤집힌다.** 한국어 화면은 명조가 제목·Cormorant 가 보조, 영어 화면은 반대.
  `AppTheme(language = ...)` 가 `LocalKoFestTypography` 를 갈아끼우므로 화면 코드는 신경 쓸 필요가 없다
- **TODO(font): 실제 폰트 파일이 저장소에 없다.** `KoFestFonts` 의 세 val 이 시스템 폴백(SansSerif/Serif)을 가리킨다.
  한글 명조가 없는 기기에서는 제목이 고딕으로 보인다. 실제 서체를 넣으려면 `core/designsystem/src/main/res/font/` 에
  파일을 넣고 `Type.kt` 의 세 val 만 바꾸면 된다 — 호출부는 전부 이 세 val 만 참조한다
- 의도적으로 **하지 않은 것**들이 `docs/03_기획_홈목록.md` 하단에 있다 — 균일한 그림자, 대문자 라벨(`FEATURED`), 버튼 안 화살표(`→`), 카드 진입 애니메이션, 정사각 지역 타일. "왜 없지" 하고 다시 넣지 않는다

## 문구 / i18n

- **`res/values-en` 을 쓰지 않는다.** 언어를 시스템 로케일이 아니라 앱 안 세그먼트로 바꾸기 때문이다.
  문구는 `:core:ui/strings` 의 `KoStrings` / `EnStrings` 에 있고 `strings()` 로 읽는다
- 원본은 `i18n/ko.json` / `en.json` 이다. 새 문구는 **먼저 원본 JSON 에 넣고** 두 Kotlin 파일에 옮긴다
- **영어는 한국어의 번역이 아니라 다시 쓴 것이다.** 문구를 추가할 때도 직역하지 않고 같은 방식으로 새로 쓴다
- 자리표시자는 JSON 표기 그대로 `{count}` 이고 `String.fill("count" to 3)` 으로 채운다
- **[미확정] 언어 전환 범위** — 지금은 **UI 와 축제 데이터를 함께** 바꾼다 (`MainActivity.themeLanguage()`).
  "UI 는 영어 유지, 목록만 한글" 로 정해지면 언어 상태를 데이터용/UI용 둘로 나누면 된다

## 작업 원칙 (`docs/04_작업원칙.md`)

이 프로젝트에서 실제로 시간을 날린 것들에서 나온 규칙이다. 일반론이 아니다.

1. **추측으로 코드를 쓰지 않는다.** 이미 세 번 되돌렸다 — `searchFestival` → 실제 `searchFestival2`, `VARCHAR(2)` 단정 → 세종시 5자리라 적재 전체 실패, 언어별 contentid 동일 가정 → 실제로는 다름. 세 번 다 **응답 한 건만 미리 봤으면** 없었을 일이다. 외부 API 를 붙일 때는 Base URL · 오퍼레이션명 · 응답 원문 1건을 먼저 확보한다
2. **문서에 `[확인됨]` / `[미확인]` 표기를 지킨다.** `[미확인]` 을 근거로 코드를 쓰지 않는다. 새로 검증한 것은 표기를 바꿔 문서에 반영한다
3. **실패를 조용히 삼키지 않는다.** `catch { }` 금지. 실패와 "데이터 없음"을 구분한다 — 둘을 같이 처리하면 재시도 기회를 잃는다. 에러 메시지는 로그뿐 아니라 **화면에도 남긴다**
4. **네트워크 실패 시 목록을 비우지 않는다.** 상단 얇은 띠로 마지막 갱신 시각만 알린다. 축제 정보는 하루 단위 갱신이라 어제 것도 대부분 맞다. (서버도 같은 이유로 응답에서 사라진 축제를 `DELETE` 하지 않고 `is_active = FALSE` 로 내린다)
5. **캐시가 아니라 저장소로 다룬다.** TTL 로 만료시키지 않는다. 있으면 쓰고, 갱신은 배치에 맡긴다. 만료 시점에 외부 의존이 생기면 외부가 죽을 때 우리도 죽는다
6. **화면보다 데이터를 먼저 확인한다.** "목록이 안 나온다" → ① DB 에 있나 ② API 가 주나 ③ 화면이 쓰나. 거꾸로 가면 화면 코드만 뒤지다 시간을 쓴다
7. **큰 것을 만들기 전에 한 건으로 검증한다**
8. **모르는 것은 모른다고 한다.** 추정으로 진행할 거면 어디까지가 추정인지 표시한다

### 에러/빈 화면 문구

- 무엇이 잘못됐고 어떻게 하면 되는지 말한다. **사과하지 않는다**
- "오류가 발생했습니다" 같은 모호한 표현을 쓰지 않는다
- 빈 화면은 사과하는 자리가 아니라 **다음 행동을 권하는 자리**다
- 결과 0건이면 **어떤 필터 때문인지** 명시하고 버튼 하나를 준다
- 로딩은 행 모양 그대로의 회색 뼈대(skeleton). 스피너를 쓰지 않는다

## 기타

- 이 디렉터리는 아직 **git 저장소가 아니다** (`git init` 이 필요하면 먼저 확인받는다)
- `applicationId` = `com.mosstis.kofest`. 모듈 namespace 는 `com.mosstis.kofest.<layer>.<module>` (예: `com.mosstis.kofest.feature.main`)
- `BaseActivity.onCreate` 는 `final` 이 아니다 — Hilt 가 `@AndroidEntryPoint` Activity 의 `onCreate` 를 override 해 `SavedStateHandle` 을 주입하기 때문이다. 하위 Activity 는 `onCreate` 대신 `ActivityContent()` / `onCreateBeforeContent` / `onCreateAfterContent` 훅을 쓴다
- 스토어 등록명은 `KoFest 코페스트` (한글 병기, "코페스트" 검색 대응)
- **로그인이 없다.** 저장(즐겨찾기)은 기기 로컬에만 둔다. 외국인 관광객이 여행 중 잠깐 쓰는 앱에서 회원가입은 이탈 지점이다. **[미확정]** — 되돌리기 어려운 결정이라 이 전제를 바꾸는 작업은 먼저 확인받는다
