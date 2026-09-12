package com.mosstis.kofest.core.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionTest {

    @Test
    fun `자리 수가 달라도 비교한다`() {
        // 앱은 "1.0", 서버는 "1.0.0" 으로 준다. 없는 자리는 0 이라 같은 버전이다.
        assertFalse(AppVersion.isOlder(current = "1.0", required = "1.0.0"))
        assertTrue(AppVersion.isOlder(current = "1.0", required = "1.0.1"))
    }

    @Test
    fun `숫자를 자리별로 본다 - 10 은 9 보다 높다`() {
        assertFalse(AppVersion.isOlder(current = "1.10.0", required = "1.9.0"))
        assertTrue(AppVersion.isOlder(current = "1.9.0", required = "1.10.0"))
    }

    @Test
    fun `꼬리표는 버리고 숫자만 본다`() {
        assertFalse(AppVersion.isOlder(current = "1.2.3-rc1", required = "1.2.3"))
    }

    @Test
    fun `모르는 값이면 업데이트를 요구하지 않는다`() {
        // versionName 을 못 읽었거나(unknown) 서버가 안 줬을 때(null)
        // 멀쩡한 앱에 "업데이트 있음" 이 붙으면 안 된다.
        assertFalse(AppVersion.isOlder(current = "unknown", required = "1.0.0"))
        assertFalse(AppVersion.isOlder(current = "1.0.0", required = null))
        assertFalse(AppVersion.isOlder(current = null, required = "1.0.0"))
        assertFalse(AppVersion.isOlder(current = "1.0.0", required = ""))
    }
}
