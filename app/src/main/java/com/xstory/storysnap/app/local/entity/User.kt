package com.xstory.storysnap.app.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey val id: Int = 0,
    val token: String,
    val isLogin: Boolean
)