package com.mosstis.kofest.data.festival.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.eventDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "events",
)

/** 아직 보내지 못한 이벤트 한 건. 서버로 나갈 때 [com.mosstis.kofest.data.festival.remote.EventDto] 가 된다 */
@Serializable
internal data class QueuedEvent(
    val event: String,
    val contentId: Long? = null,
    val at: String,
)

/**
 * 보내기 전 이벤트를 담아두는 곳.
 *
 * 메모리에만 들고 있으면 앱이 죽을 때 통째로 사라진다. 통계는 쌓여야 쓸모가 있으므로
 * 디스크에 남겨 다음 실행에서 이어 보낸다.
 *
 * [deviceId] 는 앱이 처음 만들 때 한 번 생성하는 무작위 문자열이다.
 * **광고 식별자(GAID)를 쓰지 않는다** — 개인정보 처리방침에 그렇게 적혀 있다.
 */
@Singleton
class EventQueueStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val json: Json,
) {

    private val queueKey = stringPreferencesKey("queue")
    private val deviceIdKey = stringPreferencesKey("device_id")

    /** 없으면 만들어 저장한다. 앱을 지우기 전까지 같은 값이다 */
    internal suspend fun deviceId(): String {
        var id: String? = null
        edit { preferences ->
            id = preferences[deviceIdKey] ?: UUID.randomUUID().toString().also {
                preferences[deviceIdKey] = it
            }
        }
        return id ?: UUID.randomUUID().toString()
    }

    /** 큐에 넣고 넣은 뒤의 전체 개수를 돌려준다 — 임계치를 넘었는지 호출한 쪽이 판단한다 */
    internal suspend fun add(event: QueuedEvent): Int {
        var size = 0
        edit { preferences ->
            val queue = preferences.decode() + event
            // 서버가 오래 죽어 있어도 저장소가 무한정 커지지 않게 한다. 오래된 것부터 버린다.
            val trimmed = queue.takeLast(MAX_QUEUE)
            preferences[queueKey] = json.encodeToString(trimmed)
            size = trimmed.size
        }
        return size
    }

    internal suspend fun peek(): List<QueuedEvent> {
        var queue = emptyList<QueuedEvent>()
        edit { preferences -> queue = preferences.decode() }
        return queue
    }

    /**
     * 보낸 것만 지운다. 지우는 사이에 새로 쌓인 것은 남긴다 —
     * 통째로 비우면 전송 중에 들어온 이벤트를 잃는다.
     */
    internal suspend fun remove(sent: List<QueuedEvent>) {
        if (sent.isEmpty()) return
        edit { preferences ->
            val remaining = preferences.decode().toMutableList()
            sent.forEach { remaining.remove(it) }
            preferences[queueKey] = json.encodeToString(remaining.toList())
        }
    }

    private suspend fun edit(block: (MutablePreferences) -> Unit) {
        try {
            context.eventDataStore.edit(block)
        } catch (io: IOException) {
            // 통계 때문에 앱이 죽으면 안 된다. 원인은 남기고 이번 건은 포기한다.
            android.util.Log.w(TAG, "이벤트 저장소에 쓰지 못했습니다", io)
        }
    }

    private fun Preferences.decode(): List<QueuedEvent> {
        val raw = this[queueKey] ?: return emptyList()
        return runCatching { json.decodeFromString<List<QueuedEvent>>(raw) }
            .onFailure { android.util.Log.w(TAG, "이벤트 큐를 읽지 못했습니다", it) }
            .getOrDefault(emptyList())
    }

    private companion object {
        const val TAG = "EventQueueStore"

        /** 20~30개씩 보내므로 이 정도면 며칠치다 */
        const val MAX_QUEUE = 500
    }
}
