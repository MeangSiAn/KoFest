package com.mosstis.kofest.domain.festival.repository

import com.mosstis.kofest.core.common.AppLanguage
import com.mosstis.kofest.domain.festival.model.Festival
import com.mosstis.kofest.domain.festival.model.SavedFestival
import kotlinx.coroutines.flow.Flow

/** 저장한 축제. 기기 로컬 저장소만 쓴다 */
interface SavedFestivalRepository {

    /** 최근 저장한 것이 앞에 온다 */
    fun observeSaved(): Flow<List<SavedFestival>>

    fun isSaved(language: AppLanguage, contentId: Long): Flow<Boolean>

    /** 저장돼 있으면 지우고, 아니면 저장한다. 저장된 상태를 반환한다 */
    suspend fun toggle(language: AppLanguage, festival: Festival): Boolean
}
