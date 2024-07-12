package com.xstory.storysnap.app.data.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginResponse(

    @SerializedName("loginResult")
    val loginResult: LoginResult? = null,

    @SerializedName("error")
    val error: Boolean = false,

    @SerializedName("message")
    val message: String? = null,
) : Parcelable

@Parcelize
data class LoginResult(
    @SerializedName("token")
    val token: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("name")
    val name: String,
) : Parcelable
