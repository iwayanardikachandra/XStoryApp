package com.xstory.storysnap.app.response

import com.google.gson.Gson
import com.xstory.storysnap.app.data.remote.response.StoryResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class StoryResponseTest {

    @Test
    fun `test StoryResponse`() {
        val json = """
            {
                "listStory": [
                    {
                        "id": "story-FvU4u0Vp2S3PMsFg",
                        "name": "Dimas",
                        "description": "Lorem Ipsum",
                        "photoUrl": "https://story-api.dicoding.dev/images/stories/photos-1641623658595_dummy-pic.png",
                        "createdAt": "2022-01-08T06:34:18.598Z",
                        "lat": -10.212,
                        "lon": -16.002
                    }
                ],
                "error": false,
                "message": "Stories fetched successfully"
            }
        """
        val gson = Gson()
        val response = gson.fromJson(json, StoryResponse::class.java)

        assertEquals(false, response.error)
        assertEquals("Stories fetched successfully", response.message)
        assertEquals(1, response.listStory.size)

        val story = response.listStory[0]
        assertEquals("story-FvU4u0Vp2S3PMsFg", story.id)
        assertEquals("https://story-api.dicoding.dev/images/stories/photos-1641623658595_dummy-pic.png", story.photoUrl)
        assertEquals("2022-01-08T06:34:18.598Z", story.createdAt)
        assertEquals("Dimas", story.name)
        assertEquals("Lorem Ipsum", story.description)
        assertEquals(-10.212, story.lat, -10.212)
        assertEquals(-16.002, story.lon, -16.002)
    }
}
