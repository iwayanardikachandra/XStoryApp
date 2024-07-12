package com.xstory.storysnap.app.view.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xstory.storysnap.app.data.handler.GeneralHandler
import com.xstory.storysnap.app.data.repository.MapsRepository
import com.xstory.storysnap.app.data.remote.response.StoryResponse
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MapsViewModel(private val mapsRepository: MapsRepository): ViewModel() {
    fun getStoryLocation(token: String, page: Int, size: Int): StateFlow<GeneralHandler<StoryResponse>> =
        mapsRepository.getMaps(token, page, size).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GeneralHandler.Loading
        )
}
