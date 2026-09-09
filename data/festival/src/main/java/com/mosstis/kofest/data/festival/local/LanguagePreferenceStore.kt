package com.mosstis.kofest.data.festival.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mosstis.kofest.core.common.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.languageDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "language",
)

/**
 * 선택한 언어. **기기에만 저장하고 서버로 보내지 않는다** — 계정이 없다.
 *
 * 아직 고르지 않았으면 기기 언어로 기본값을 정한다: 한국어 기기면 한국어, 그 외는 전부 English.
 * 일본어·중국어 기기도 English 인 이유는 축제 데이터가 일본어 1건, 중문 극소량이라 넣지 않았기 때문이다.
 * 다만 이 값은 **화면에 미리 채워 보여줄 기본 선택일 뿐**이고,
 * 사용자가 직접 고르기 전까지 [hasSelected] 는 false 다.
 */
@Singleton
class LanguagePreferenceStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    private val languageKey = stringPreferencesKey("language")
    private val selectedKey = booleanPreferencesKey("selected")

    private val preferences: Flow<Preferences> = context.languageDataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                android.util.Log.w(TAG, "언어 설정을 읽지 못했습니다", throwable)
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }

    val language: Flow<AppLanguage> = preferences.map { stored ->
        stored[languageKey]?.let(AppLanguage::from) ?: deviceDefault()
    }

    val hasSelected: Flow<Boolean> = preferences.map { it[selectedKey] == true }

    /** 기기 언어로 정한 기본 선택. 저장된 값이 아니다 */
    fun deviceDefault(): AppLanguage =
        if (Locale.getDefault().language == KOREAN) AppLanguage.KO else AppLanguage.EN

    suspend fun set(language: AppLanguage) {
        context.languageDataStore.edit { preferences ->
            preferences[languageKey] = language.code
            preferences[selectedKey] = true
        }
    }

    private companion object {
        const val TAG = "LanguagePreferenceStore"

        /** `Locale("ko").language` 는 "ko" 다 */
        const val KOREAN = "ko"
    }
}
