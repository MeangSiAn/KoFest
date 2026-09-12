package com.mosstis.kofest.data.festival.analytics

import com.mosstis.kofest.data.festival.local.EventQueueStore
import com.mosstis.kofest.data.festival.local.LanguagePreferenceStore
import com.mosstis.kofest.data.festival.local.QueuedEvent
import com.mosstis.kofest.data.festival.remote.EventBatchRequest
import com.mosstis.kofest.data.festival.remote.EventDto
import com.mosstis.kofest.data.festival.remote.KoFestApi
import com.mosstis.kofest.data.festival.remote.apiCall
import com.mosstis.kofest.domain.festival.analytics.AppEvent
import com.mosstis.kofest.domain.festival.analytics.EventTracker
import com.mosstis.kofest.domain.festival.repository.AppVersionProvider
import com.mosstis.kofest.domain.festival.repository.FestivalDataException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 이벤트를 모았다가 [BATCH_SIZE] 개가 되면 올린다.
 *
 * 통계가 앱을 방해하지 않는 것이 첫째 규칙이다 —
 * [track] 은 기다리지 않고, 전송이 실패해도 화면에는 아무 일도 일어나지 않는다.
 * 다만 **조용히 삼키지는 않는다.** 실패는 로그로 남기고 큐는 그대로 둬서 다음에 다시 보낸다.
 */
@Singleton
class QueuedEventTracker @Inject constructor(
    private val api: KoFestApi,
    private val json: Json,
    private val store: EventQueueStore,
    private val languageStore: LanguagePreferenceStore,
    private val appVersion: AppVersionProvider,
) : EventTracker {

    /** 화면 생명주기와 무관하게 살아 있어야 한다 — 화면이 사라져도 보내던 것은 마저 보낸다 */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** 전송이 겹치면 같은 이벤트를 두 번 보낸다 */
    private val sending = Mutex()

    override fun track(event: AppEvent, contentId: Long?) {
        scope.launch {
            val queued = QueuedEvent(
                event = event.eventName,
                contentId = contentId,
                at = Instant.now().toString(),
            )
            if (store.add(queued) >= BATCH_SIZE) send()
        }
    }

    override fun flush() {
        scope.launch { send() }
    }

    private suspend fun send() = sending.withLock {
        val batch = store.peek().take(MAX_BATCH)
        if (batch.isEmpty()) return@withLock

        try {
            val request = EventBatchRequest(
                deviceId = store.deviceId(),
                appVersion = appVersion.versionName,
                lang = languageStore.language.first().code,
                events = batch.map { EventDto(event = it.event, contentId = it.contentId, at = it.at) },
            )
            val accepted = apiCall(json) { api.postEvents(request) }.accepted
            // 보낸 수와 받은 수가 다르면 서버가 일부를 버린 것이다. 큐에서는 지우되 원인은 남긴다.
            if (accepted != batch.size) {
                android.util.Log.w(TAG, "이벤트 ${batch.size}건을 보냈는데 ${accepted}건만 받았습니다")
            }
            store.remove(batch)
        } catch (failure: FestivalDataException) {
            // 큐를 비우지 않는다. 네트워크가 돌아오면 다음 이벤트에 얹혀 다시 나간다.
            android.util.Log.w(TAG, "이벤트 ${batch.size}건 전송 실패", failure)
        }
    }

    private companion object {
        const val TAG = "QueuedEventTracker"

        /** 이만큼 쌓이면 보낸다 (개발문서 3장: 20~30개) */
        const val BATCH_SIZE = 20

        /** 한 번에 보내는 상한. 오래 못 보낸 큐를 통째로 올리지 않는다 */
        const val MAX_BATCH = 30
    }
}
