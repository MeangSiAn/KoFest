package com.mosstis.kofest.data.festival.di

import com.mosstis.kofest.data.festival.analytics.QueuedEventTracker
import com.mosstis.kofest.data.festival.local.AndroidAppVersionProvider
import com.mosstis.kofest.data.festival.local.AndroidLocationProvider
import com.mosstis.kofest.data.festival.repository.LocalSavedFestivalRepository
import com.mosstis.kofest.data.festival.repository.RemoteDiscoverRepository
import com.mosstis.kofest.data.festival.repository.RemoteFestivalRepository
import com.mosstis.kofest.domain.festival.analytics.EventTracker
import com.mosstis.kofest.domain.festival.repository.AppVersionProvider
import com.mosstis.kofest.domain.festival.repository.FestivalRepository
import com.mosstis.kofest.domain.festival.repository.LocationProvider
import com.mosstis.kofest.domain.festival.repository.PickRepository
import com.mosstis.kofest.domain.festival.repository.PlaceRepository
import com.mosstis.kofest.domain.festival.repository.PlanRepository
import com.mosstis.kofest.domain.festival.repository.StoryRepository
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

    @Binds
    @Singleton
    abstract fun bindEventTracker(impl: QueuedEventTracker): EventTracker

    @Binds
    @Singleton
    abstract fun bindAppVersionProvider(impl: AndroidAppVersionProvider): AppVersionProvider

    // 관광지·매거진·일정은 구현체 하나가 셋을 다 맡는다. 같은 API 를 쓰고
    // 서버가 열리는 시점도 같아서, 나누면 파일만 늘고 얻는 게 없다.
    @Binds
    @Singleton
    abstract fun bindPlaceRepository(impl: RemoteDiscoverRepository): PlaceRepository

    @Binds
    @Singleton
    abstract fun bindStoryRepository(impl: RemoteDiscoverRepository): StoryRepository

    @Binds
    @Singleton
    abstract fun bindPlanRepository(impl: RemoteDiscoverRepository): PlanRepository

    @Binds
    @Singleton
    abstract fun bindPickRepository(impl: RemoteDiscoverRepository): PickRepository

    @Binds
    @Singleton
    abstract fun bindLocationProvider(impl: AndroidLocationProvider): LocationProvider
}
