package com.mosstis.kofest.data.festival.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.savedFestivalDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "saved_festivals",
)

/**
 * 저장한 축제 목록. 기기에만 남고 서버로 가지 않는다.
 *
 * 읽기 실패(IOException)를 삼키지 않되, 저장 탭이 통째로 죽지 않도록
 * 빈 목록으로 흘려보내고 로그를 남긴다.
 */
@Singleton
class SavedFestivalStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val json: Json,
) {

    private val key = stringPreferencesKey("items")

    internal val saved: Flow<List<SavedFestivalEntity>> = context.savedFestivalDataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                android.util.Log.w(TAG, "저장 목록을 읽지 못했습니다", throwable)
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences -> preferences.decode() }

    internal suspend fun toggle(entity: SavedFestivalEntity): Boolean {
        var nowSaved = false
        context.savedFestivalDataStore.edit { preferences ->
            val current = preferences.decode()
            val existing = current.firstOrNull {
                it.lang == entity.lang && it.contentId == entity.contentId
            }
            val updated = if (existing != null) {
                current - existing
            } else {
                nowSaved = true
                listOf(entity) + current
            }
            preferences[key] = json.encodeToString(updated)
        }
        return nowSaved
    }

    private fun Preferences.decode(): List<SavedFestivalEntity> {
        val raw = this[key] ?: return emptyList()
        return runCatching {
            json.decodeFromString<List<SavedFestivalEntity>>(raw)
        }.getOrElse { throwable ->
            // 형식이 바뀌었거나 깨졌다. 앱을 죽이지 않고 비운 뒤 원인을 남긴다.
            android.util.Log.w(TAG, "저장 목록을 해석하지 못했습니다", throwable)
            emptyList()
        }
    }

    private companion object {
        const val TAG = "SavedFestivalStore"
    }
}
