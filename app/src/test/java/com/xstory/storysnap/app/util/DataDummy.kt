package com.xstory.storysnap.app.util

import com.xstory.storysnap.app.data.remote.response.ListStoryItem
import com.xstory.storysnap.app.data.remote.response.LoginResponse
import com.xstory.storysnap.app.data.remote.response.LoginResult
import com.xstory.storysnap.app.data.remote.response.RegisterResponse

object DataDummy {
    fun pagingListStory(): List<ListStoryItem> {
        val items = arrayListOf<ListStoryItem>()
        for (x in 0..100) {
            val story = ListStoryItem(
                id = "storyId-$x",
                photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1641623658595_dummy-pic$x.png",
                createdAt = "2022-01-08T06:34:18.598Z",
                name = "User $x",
                description = "Lorem Ipsum",
                lat = -16.002 + x,
                lon = -10.212 - x
            )
            items.add(story)
        }
        return items
    }
}