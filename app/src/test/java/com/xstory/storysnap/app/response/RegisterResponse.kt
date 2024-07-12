package com.xstory.storysnap.app.response

import com.google.gson.Gson
import com.xstory.storysnap.app.data.remote.response.RegisterResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class RegisterResponseTest {

    @Test
    fun `test RegisterResponse`() {
        val json = """
            {
                "error": false,
                "message": "User Created"
            }
        """
        val gson = Gson()
        val response = gson.fromJson(json, RegisterResponse::class.java)

        assertEquals(false, response.error)
        assertEquals("User Created", response.message)
    }
}
