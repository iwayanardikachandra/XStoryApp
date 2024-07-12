package com.xstory.storysnap.app.view.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityLoginBinding
import com.xstory.storysnap.app.data.handler.GeneralHandler
import com.xstory.storysnap.app.data.remote.response.LoginResponse
import com.xstory.storysnap.app.view.home.HomeActivity
import com.xstory.storysnap.app.data.factory.ViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels {
        ViewModelFactory.getUserInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupViewModel()
        setupAction()
        playAnimation()
    }

    private fun setupView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupViewModel() {
        lifecycleScope.launchWhenStarted {
            loginViewModel.getUser().collect { token ->
                if (!token.isNullOrEmpty()) {
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (validateInput(email, password)) {
                clearErrors()
                showLoading(true)

                lifecycleScope.launch {
                    loginViewModel.login(email, password)
                    loginViewModel.loginState.collect { result ->
                        handleLoginResult(result)
                    }
                }
            }
        }
    }

    private fun clearErrors() {
        binding.emailEditTextLayout.error = null
        binding.passwordEditTextLayout.error = null
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true
        if (email.isEmpty()) {
            binding.emailEditTextLayout.error = getString(R.string.message_validation, "email")
            isValid = false
        }
        if (password.isEmpty() || password.length < 8) {
            binding.passwordEditTextLayout.error = getString(R.string.error_password)
            isValid = false
        }
        return isValid
    }

    private fun handleLoginResult(result: GeneralHandler<LoginResponse>) {
        when (result) {
            is GeneralHandler.Loading -> showLoading(true)
            is GeneralHandler.Success -> handleSuccess(result.data)
            is GeneralHandler.Error -> showError(result.error)
            else -> {
                //nothing
            }
        }
    }

    private fun handleSuccess(user: LoginResponse) {
        showLoading(false)
        if (user.error) {
            user.message?.let { showAlert(it) }
        } else {
            val token = user.loginResult?.token.orEmpty()
            lifecycleScope.launch {
                loginViewModel.setToken(token, true)
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finish()
            }
        }
    }

    private fun showError(message: String?) {
        showLoading(false)
        showAlert(message ?: getString(R.string.login_error))
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            emailEditText.isEnabled = !isLoading
            passwordEditText.isEnabled = !isLoading
            loginButton.isEnabled = !isLoading
            progressBar.animateVisibility(isLoading)
        }
    }

    private fun View.animateVisibility(isVisible: Boolean, duration: Long = 800) {
        clearAnimation()
        if (isVisible) {
            visibility = View.VISIBLE
            alpha = 0f

            animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null)
                .start()
        } else {
            animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        visibility = View.GONE
                        alpha = 1f
                    }
                })
                .start()
        }
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.login_error))
            setMessage(message)
            setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
    }

    private fun playAnimation() {
        val animations = listOf(
            ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 0f, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 0f, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 0f, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 0f, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 0f, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 0f, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 0f, 1f).setDuration(100)
        )

        AnimatorSet().apply {
            playSequentially(animations)
            startDelay = 100
        }.start()

        ObjectAnimator.ofFloat(binding.imageView, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()
    }
}
