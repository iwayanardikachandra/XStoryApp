package com.xstory.storysnap.app.data.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.xstory.storysnap.app.di.Injection
import com.xstory.storysnap.app.data.repository.MapsRepository
import com.xstory.storysnap.app.data.repository.UserRepository
import com.xstory.storysnap.app.data.remote.ApiService
import com.xstory.storysnap.app.data.repository.StoryRepository
import com.xstory.storysnap.app.view.login.LoginViewModel
import com.xstory.storysnap.app.view.home.HomeViewModel
import com.xstory.storysnap.app.view.maps.MapsViewModel
import com.xstory.storysnap.app.view.signup.SignUpViewModel
import com.xstory.storysnap.app.view.story.StoryViewModel

class ViewModelFactory private constructor(
    private val userRepository: UserRepository,
    private val storyRepository: StoryRepository? = null,
    private val mapsRepository: MapsRepository? = null,
    private val apiService: ApiService? = null,
) : ViewModelProvider.NewInstanceFactory() {

    companion object {
        private var userFactoryInstance: ViewModelFactory? = null
        private var storyFactoryInstance: ViewModelFactory? = null
        private var mapsFactoryInstance: ViewModelFactory? = null

        fun getUserInstance(context: Context): ViewModelFactory {
            return userFactoryInstance ?: synchronized(this) {
                userFactoryInstance ?: ViewModelFactory(
                    Injection.provideAuthRepository(context)
                ).also {
                    userFactoryInstance = it
                }
            }
        }

        fun getStoryInstance(context: Context): ViewModelFactory {
            return storyFactoryInstance ?: synchronized(this) {
                storyFactoryInstance ?: ViewModelFactory(
                    Injection.provideAuthRepository(context),
                    Injection.provideStoryRepository(context),
                    null,
                    Injection.provideApiService()
                ).also {
                    storyFactoryInstance = it
                }
            }
        }

        fun getMapsInstance(context: Context): ViewModelFactory {
            return mapsFactoryInstance ?: synchronized(this) {
                mapsFactoryInstance ?: ViewModelFactory(
                    Injection.provideAuthRepository(context),
                    null,
                    Injection.provideMapsRepository(),
                    Injection.provideApiService()
                ).also {
                    mapsFactoryInstance = it
                }
            }
        }
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(userRepository)
            modelClass.isAssignableFrom(SignUpViewModel::class.java) -> SignUpViewModel(
                userRepository
            )

            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(
                userRepository,
                storyRepository!!
            )

            modelClass.isAssignableFrom(StoryViewModel::class.java) -> StoryViewModel(apiService!!)
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> MapsViewModel(mapsRepository!!)
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        } as T
    }
}
