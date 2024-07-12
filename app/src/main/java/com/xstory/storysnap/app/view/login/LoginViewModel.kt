package com.xstory.storysnap.app.view.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xstory.storysnap.app.data.handler.GeneralHandler
import com.xstory.storysnap.app.data.repository.UserRepository
import com.xstory.storysnap.app.data.remote.response.LoginResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LoginViewModel(private val repo: UserRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<GeneralHandler<LoginResponse>>(GeneralHandler.Idle)
    val loginState: StateFlow<GeneralHandler<LoginResponse>> get() = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            repo.login(email, password)
                .catch { exception ->
                    _loginState.value = GeneralHandler.Error(exception.localizedMessage ?: "Unknown error occurred")
                }
                .collect { result ->
                    _loginState.value = result
                }
        }
    }

    fun getUser(): Flow<String?> {
        return repo.getUser().map { it?.token }
    }

    fun setToken(token: String, isLogin: Boolean) {
        viewModelScope.launch {
            repo.setToken(token, isLogin)
        }
    }
}