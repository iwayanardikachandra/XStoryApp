package com.xstory.storysnap.app.view.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivitySignupBinding
import com.xstory.storysnap.app.data.handler.GeneralHandler
import com.xstory.storysnap.app.data.remote.response.RegisterResponse
import com.xstory.storysnap.app.data.factory.ViewModelFactory
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val signUpViewModel: SignUpViewModel by viewModels {
        ViewModelFactory.getUserInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        playAnimation()
        setupViewModel()
        setupAction()
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
            signUpViewModel.signUpState.collect { state ->
                handleSignUpResult(state)
            }
        }
    }

    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (validateInput(name, email, password)) {
                lifecycleScope.launch {
                    signUpViewModel.saveUser(name, email, password)
                }
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String): Boolean {
        return when {
            name.isEmpty() -> {
                binding.nameEditTextLayout.error = getString(R.string.message_validation, "name")
                false
            }
            email.isEmpty() -> {
                binding.emailEditTextLayout.error = getString(R.string.message_validation, "email")
                false
            }
            password.isEmpty() || password.length <= 7 -> {
                binding.passwordEditTextLayout.error = getString(R.string.error_password)
                false
            }
            else -> true
        }
    }

    private fun handleSignUpResult(result: GeneralHandler<RegisterResponse>) {
        when (result) {
            is GeneralHandler.Loading -> showLoading(true)
            is GeneralHandler.Success -> {
                showLoading(false)
                result.data?.let { handleSignUpSuccess(it) }
            }
            is GeneralHandler.Error -> {
                showLoading(false)
                showAlert(result.error)
            }
            else -> {}
        }
    }

    private fun handleSignUpSuccess(user: RegisterResponse) {
        if (user.error) {
            showAlert(user.message)
        } else {
            AlertDialog.Builder(this@SignupActivity).apply {
                setTitle("Yeah!")
                setMessage("Akun berhasil dibuat, yuk segera login")
                setPositiveButton("Next") { _, _ -> finish() }
                create()
                show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            emailEditText.isEnabled = !isLoading
            passwordEditText.isEnabled = !isLoading
            nameEditText.isEnabled = !isLoading
            signupButton.isEnabled = !isLoading

            progressBar.animateVisibility(isLoading)
        }
    }

    private fun View.animateVisibility(isVisible: Boolean, duration: Long = 400) {
        if (isVisible) {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(duration).start()
        } else {
            animate().alpha(0f).setDuration(duration).withEndAction {
                visibility = View.GONE
            }.start()
        }
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.signup_error))
            setMessage(message)
            setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
    }

    private fun playAnimation() {
        val animations = listOf(
            ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 0f, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.nameTextView, View.ALPHA, 0f, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 0f, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 0f, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 0f, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 0f, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 0f, 1f).setDuration(100),
            ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 0f, 1f).setDuration(100)
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
