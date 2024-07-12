package com.xstory.storysnap.app.local.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "story_preferences")

class StoryPreferences(private val context: Context) {

    companion object {
        val STORY_KEY = stringPreferencesKey("story_key")
    }

    suspend fun saveStory(story: String) {
        context.dataStore.edit { preferences ->
            preferences[STORY_KEY] = story
        }
    }
}
