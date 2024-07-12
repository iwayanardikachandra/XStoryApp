package com.xstory.storysnap.app.response

import com.google.gson.Gson
import com.xstory.storysnap.app.data.remote.response.LoginResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginResponseTest {

    @Test
    fun `test LoginResponse`() {
        val json = """
            {
                "loginResult": {
                    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLXlqNXBjX0xBUkNfQWdLNjEiLCJpYXQiOjE2NDE3OTk5NDl9.flEMaQ7zsdYkxuyGbiXjEDXO8kuDTcI__3UjCwt6R_I"
                },
                "error": false,
                "message": "Success"
            }
        """
        val gson = Gson()
        val response = gson.fromJson(json, LoginResponse::class.java)

        assertEquals(false, response.error)
        assertEquals("Success", response.message)
        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLXlqNXBjX0xBUkNfQWdLNjEiLCJpYXQiOjE2NDE3OTk5NDl9.flEMaQ7zsdYkxuyGbiXjEDXO8kuDTcI__3UjCwt6R_I", response.loginResult?.token)
    }
}
