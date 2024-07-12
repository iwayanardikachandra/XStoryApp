package com.xstory.storysnap.app.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.xstory.storysnap.app.local.entity.Keys
import com.xstory.storysnap.app.local.room.StoryDatabase
import com.xstory.storysnap.app.data.remote.ApiService
import com.xstory.storysnap.app.data.remote.response.ListStoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPagingApi::class)
class StoryMediator(
    private val apiService: ApiService,
    private val database: StoryDatabase,
    private val authToken: String,
) : RemoteMediator<Int, ListStoryItem>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
        const val CACHE_EXPIRATION_TIME = 60 * 60 * 1000
    }

    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = System.currentTimeMillis() - CACHE_EXPIRATION_TIME
        val remoteKeys = database.remoteKeysDao().getRemoteKeysId("1")
        return if (remoteKeys == null || remoteKeys.lastUpdated < cacheTimeout) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryItem>,
    ): MediatorResult = withContext(Dispatchers.IO) {
        val page = when (loadType) {
            LoadType.REFRESH -> state.getClosestRemoteKeys(database)?.nextKey?.minus(1)
                ?: INITIAL_PAGE_INDEX

            LoadType.PREPEND -> state.getFirstRemoteKeys(database)?.prevKey
                ?: return@withContext MediatorResult.Success(
                    endOfPaginationReached = state.getFirstRemoteKeys(
                        database
                    ) != null
                )

            LoadType.APPEND -> state.getLastRemoteKeys(database)?.nextKey
                ?: return@withContext MediatorResult.Success(
                    endOfPaginationReached = state.getLastRemoteKeys(
                        database
                    ) != null
                )
        }

        return@withContext runCatching {
            val response = apiService.getStories("Bearer $authToken", page, state.config.pageSize)
            val stories = response.listStory
            val endOfPaginationReached = stories.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().deleteRemoteKeys()
                    database.storyDao().deleteAll()
                }

                val prevKey = if (page == INITIAL_PAGE_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val currentTime = System.currentTimeMillis()

                val keys = stories.map {
                    Keys(
                        id = it.id,
                        prevKey = prevKey,
                        nextKey = nextKey,
                        lastUpdated = currentTime
                    )
                }

                database.remoteKeysDao().insertAll(keys)
                database.storyDao().insertStory(stories)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        }.getOrElse { exception ->
            MediatorResult.Error(exception)
        }
    }

    private suspend fun PagingState<Int, ListStoryItem>.getFirstRemoteKeys(database: StoryDatabase): Keys? {
        return pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { story ->
            database.remoteKeysDao().getRemoteKeysId(story.id)
        }
    }

    private suspend fun PagingState<Int, ListStoryItem>.getLastRemoteKeys(database: StoryDatabase): Keys? {
        return pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { story ->
            database.remoteKeysDao().getRemoteKeysId(story.id)
        }
    }

    private suspend fun PagingState<Int, ListStoryItem>.getClosestRemoteKeys(database: StoryDatabase): Keys? {
        return anchorPosition?.let { position ->
            closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeysDao().getRemoteKeysId(id)
            }
        }
    }
}
