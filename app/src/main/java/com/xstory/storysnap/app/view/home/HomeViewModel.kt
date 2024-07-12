package com.xstory.storysnap.app.view.home

import androidx.lifecycle.*
import androidx.paging.PagingData
import com.xstory.storysnap.app.data.remote.response.ListStoryItem
import com.xstory.storysnap.app.data.repository.UserRepository
import com.xstory.storysnap.app.data.repository.StoryRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepo: UserRepository,
    private val storyRepo: StoryRepository,
) : ViewModel() {

    val loginState: LiveData<Boolean>
        get() = userRepo.isLogin().asLiveData()

    val userToken: LiveData<String?>
        get() = userRepo.getUser().map { it?.token }.asLiveData()

    fun fetchStories(token: String): LiveData<PagingData<ListStoryItem>> {
        return storyRepo.getStories(token)
    }

    fun logout() {
        viewModelScope.launch {
            userRepo.logout()
        }
    }
}
