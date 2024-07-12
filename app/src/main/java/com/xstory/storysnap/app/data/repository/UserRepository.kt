package com.xstory.storysnap.app.data.repository

import com.xstory.storysnap.app.local.entity.User
import com.xstory.storysnap.app.local.room.UserDao
import com.xstory.storysnap.app.data.remote.ApiService
import com.xstory.storysnap.app.data.handler.GeneralHandler
import com.xstory.storysnap.app.data.remote.response.LoginResponse
import com.xstory.storysnap.app.data.remote.response.RegisterResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import kotlin.coroutines.CoroutineContext

class UserRepository private constructor(
    private val userDao: UserDao,
    private val apiService: ApiService,
    private val ioContext: CoroutineContext
) {

    fun register(name: String, email: String, password: String): Flow<GeneralHandler<RegisterResponse>> = flow {
        emit(GeneralHandler.Loading)
        try {
            val result = apiService.register(name, email, password)
            emit(GeneralHandler.Success(result))
        } catch (exception: Exception) {
            emit(GeneralHandler.Error(getErrorMessage(exception)))
        }
    }.flowOn(ioContext)

    fun login(email: String, password: String): Flow<GeneralHandler<LoginResponse>> = flow {
        emit(GeneralHandler.Loading)
        try {
            val result = apiService.login(email, password)
            result.loginResult?.token?.let { token ->
                saveUser(token)
            }
            emit(GeneralHandler.Success(result))
        } catch (exception: Exception) {
            emit(GeneralHandler.Error(getErrorMessage(exception)))
        }
    }.flowOn(ioContext)

    private suspend fun saveUser(token: String) {
        withContext(ioContext) {
            userDao.insertUser(User(token = token, isLogin = true))
        }
    }

    fun getUser(): Flow<User?> = userDao.getUser()

    fun isLogin(): Flow<Boolean> {
        return userDao.getUser().map { user -> user?.isLogin ?: false }
    }

    suspend fun setToken(token: String, isLogin: Boolean) {
        withContext(ioContext) {
            userDao.insertUser(User(token = token, isLogin = isLogin))
        }
    }

    suspend fun logout() {
        withContext(ioContext) {
            userDao.deleteUser()
        }
    }

    private fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is HttpException -> handleHttpException(exception)
            else -> exception.message ?: "Unknown error occurred"
        }
    }

    private fun handleHttpException(throwable: HttpException): String {
        return try {
            val errorBody = throwable.response()?.errorBody()?.string()
            errorBody ?: "Unknown error occurred"
        } catch (exception: Exception) {
            exception.message ?: "Unknown error occurred"
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(userDao: UserDao, apiService: ApiService, ioContext: CoroutineContext): UserRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = UserRepository(userDao, apiService, ioContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
