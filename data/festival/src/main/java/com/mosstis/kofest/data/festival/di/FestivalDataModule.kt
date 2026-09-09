package com.mosstis.kofest.data.festival.di

import com.mosstis.kofest.data.festival.repository.LocalSavedFestivalRepository
import com.mosstis.kofest.data.festival.repository.RemoteFestivalRepository
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.repository.SavedFestivalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository 구현체 조립. feature 는 인터페이스만 알고 구현은 여기서 묶는다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FestivalDataModule {

    @Binds
    @Singleton
    abstract fun bindFestivalRepository(impl: RemoteFestivalRepository): FestivalRepository

    @Binds
    @Singleton
    abstract fun bindSavedFestivalRepository(
        impl: LocalSavedFestivalRepository,
    ): SavedFestivalRepository
}
