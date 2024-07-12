package com.xstory.storysnap.app.view.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xstory.storysnap.app.data.handler.GeneralHandler
import com.xstory.storysnap.app.data.remote.ApiService
import com.xstory.storysnap.app.data.remote.response.AddStoryResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import kotlinx.coroutines.Dispatchers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class StoryViewModel(private val apiService: ApiService) : ViewModel() {

    private val _uploadStatus = MutableSharedFlow<GeneralHandler<AddStoryResponse>>()
    val uploadStatus = _uploadStatus.asSharedFlow()

    fun uploadStory(
        token: String,
        imageMultipart: MultipartBody.Part,
        desc: String,
        latitude: Double?,
        longitude: Double?
    ) {
        viewModelScope.launch {
            flow {
                emit(GeneralHandler.Loading)
                try {
                    val descRequestBody = desc.toRequestBody("text/plain".toMediaTypeOrNull())
                    val response = apiService.uploadStory(token, imageMultipart, descRequestBody, latitude, longitude)
                    emit(GeneralHandler.Success(response))
                } catch (e: Exception) {
                    emit(GeneralHandler.Error(e.message ?: "Unknown error"))
                }
            }.flowOn(Dispatchers.IO).collect {
                _uploadStatus.emit(it)
            }
        }
    }
}
