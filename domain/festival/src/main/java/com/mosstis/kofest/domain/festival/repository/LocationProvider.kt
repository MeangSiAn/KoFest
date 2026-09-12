package com.mosstis.kofest.domain.festival.repository

/** 위도·경도. 오늘 뽑기에 쓰고 버린다 — 어디에도 저장하지 않는다 */
data class GeoPoint(val latitude: Double, val longitude: Double)

/**
 * 기기의 현재 위치. 구현은 `:data:festival` 에서 `LocationManager` 로 읽는다.
 *
 * 권한은 **버튼을 누를 때** 묻는다 (기획서 11) — 그 대화상자는 화면(Route)이 띄우고,
 * 여기는 허락된 뒤에 좌표만 준다.
 */
interface LocationProvider {
    /** 위치 권한(대략 위치라도)을 이미 허락했는가. 두 번째부터는 안내 화면을 건너뛰기 위한 것 */
    fun hasPermission(): Boolean

    /** 현재 위치. 권한이 없거나 제한 시간 안에 못 잡으면 null */
    suspend fun current(): GeoPoint?
}
