package com.xstory.storysnap.app.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.xstory.storysnap.app.local.room.StoryDatabase
import com.xstory.storysnap.app.data.remote.ApiService
import com.xstory.storysnap.app.data.paging.StoryMediator
import com.xstory.storysnap.app.data.remote.response.ListStoryItem

class StoryRepository(
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase,
) {
    @OptIn(ExperimentalPagingApi::class)
    fun getStories(token: String): LiveData<PagingData<ListStoryItem>> = Pager(
        config = PagingConfig(pageSize = 5),
        remoteMediator = StoryMediator(apiService, storyDatabase, token),
        pagingSourceFactory = { storyDatabase.storyDao().getAllStories() }
    ).liveData
}
