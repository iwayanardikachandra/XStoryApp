package com.xstory.storysnap.app.view.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xstory.storysnap.app.data.handler.GeneralHandler
import com.xstory.storysnap.app.data.repository.UserRepository
import com.xstory.storysnap.app.data.remote.response.RegisterResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SignUpViewModel(private val repo: UserRepository) : ViewModel() {

    private val _signUpState = MutableStateFlow<GeneralHandler<RegisterResponse>>(GeneralHandler.Idle)
    val signUpState: StateFlow<GeneralHandler<RegisterResponse>> get() = _signUpState

    fun saveUser(name: String, email: String, password: String) {
        viewModelScope.launch {
            repo.register(name, email, password)
                .catch { exception ->
                    _signUpState.value = GeneralHandler.Error(exception.localizedMessage ?: "Unknown error occurred")
                }
                .collect { result ->
                    _signUpState.value = result
                }
        }
    }
}