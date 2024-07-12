package com.xstory.storysnap.app.di

import android.content.Context
import com.xstory.storysnap.app.local.room.UserDatabase
import com.xstory.storysnap.app.local.room.StoryDatabase
import com.xstory.storysnap.app.data.remote.ApiConnect
import com.xstory.storysnap.app.data.remote.ApiService
import com.xstory.storysnap.app.data.repository.MapsRepository
import com.xstory.storysnap.app.data.repository.UserRepository
import com.xstory.storysnap.app.data.repository.StoryRepository
import kotlinx.coroutines.Dispatchers

object Injection {

    fun provideApiService(): ApiService {
        return ApiConnect.getApiService()
    }

    fun provideAuthRepository(context: Context): UserRepository {
        val apiService = ApiConnect.getApiService()
        val database = UserDatabase.getDatabase(context)
        val userDao = database.userDao()
        return UserRepository.getInstance(userDao, apiService, Dispatchers.IO)
    }

    fun provideStoryRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConnect.getApiService()
        return StoryRepository(apiService, database)
    }

    fun provideMapsRepository(): MapsRepository {
        val apiService = ApiConnect.getApiService()
        return MapsRepository(apiService)
    }
}
