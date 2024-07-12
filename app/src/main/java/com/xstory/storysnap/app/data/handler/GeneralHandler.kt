package com.xstory.storysnap.app.data.handler

sealed class GeneralHandler<out R>{
    object Idle : GeneralHandler<Nothing>()
    object Loading : GeneralHandler<Nothing>()
    data class Success<out T>(val data: T) : GeneralHandler<T>()
    data class Error(val error: String) : GeneralHandler<Nothing>()
}