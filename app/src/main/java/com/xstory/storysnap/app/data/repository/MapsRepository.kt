package com.xstory.storysnap.app.data.repository

import kotlinx.coroutines.Dispatchers
import com.xstory.storysnap.app.data.remote.ApiService
import com.xstory.storysnap.app.data.handler.GeneralHandler
import com.xstory.storysnap.app.data.remote.response.StoryResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MapsRepository(
    private val apiService: ApiService,
) {
    fun getMaps(token: String, page: Int, size: Int): Flow<GeneralHandler<StoryResponse>> = flow {
        try {
            emit(GeneralHandler.Loading)
            val response =
                apiService.getStories("Bearer $token", page = page, size = size, location = 1)
            emit(GeneralHandler.Success(response))
        } catch (e: Exception) {
            emit(GeneralHandler.Error(e.message.toString()))
        }
    }.flowOn(Dispatchers.IO)
}
