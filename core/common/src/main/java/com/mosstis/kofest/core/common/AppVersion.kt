package com.mosstis.kofest.core.common

/**
 * `1.0` · `1.0.0` · `1.2.3-rc1` 같은 버전 문자열 비교.
 *
 * 서버가 주는 `minAppVersion` 과 앱의 `versionName` 은 자리 수가 다를 수 있다
 * (앱 "1.0" vs 서버 "1.0.0"). 없는 자리는 0 으로 보고, 숫자가 아닌 꼬리표는 버린다.
 * 숫자를 하나도 못 읽으면 비교하지 않는다 — 모르는 값으로 "업데이트 필요"를 띄우면
 * 멀쩡한 앱에 경고가 붙는다.
 */
object AppVersion {

    /** [current] 가 [required] 보다 낮으면 true. 둘 중 하나라도 못 읽으면 false */
    fun isOlder(current: String?, required: String?): Boolean {
        val left = parse(current) ?: return false
        val right = parse(required) ?: return false
        val size = maxOf(left.size, right.size)
        for (index in 0 until size) {
            val a = left.getOrElse(index) { 0 }
            val b = right.getOrElse(index) { 0 }
            if (a != b) return a < b
        }
        return false
    }

    private fun parse(version: String?): List<Int>? {
        if (version.isNullOrBlank()) return null
        val numbers = version.trim()
            .substringBefore('-')
            .substringBefore('+')
            .split('.')
            .map { part -> part.takeWhile(Char::isDigit).toIntOrNull() ?: return null }
        return numbers.ifEmpty { null }
    }
}
