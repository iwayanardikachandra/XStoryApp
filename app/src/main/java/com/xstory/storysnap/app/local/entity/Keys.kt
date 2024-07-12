package com.xstory.storysnap.app.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class Keys(
    @PrimaryKey val id: String,
    val prevKey: Int?,
    val nextKey: Int?,
    val lastUpdated: Long
)
