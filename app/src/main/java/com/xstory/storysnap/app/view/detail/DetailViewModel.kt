package com.xstory.storysnap.app.view.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xstory.storysnap.app.data.remote.response.ListStoryItem

class DetailViewModel : ViewModel() {

    private val storyModel = MutableLiveData<ListStoryItem>()
    val story: LiveData<ListStoryItem> get() = storyModel

    fun setStory(story: ListStoryItem) {
        storyModel.value = story
    }
}