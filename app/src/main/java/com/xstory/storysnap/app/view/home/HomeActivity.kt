package com.xstory.storysnap.app.view.home

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.xstory.storysnap.app.local.pref.StoryPreferences
import com.xstory.storysnap.app.data.factory.ViewModelFactory
import com.xstory.storysnap.app.view.maps.MapsActivity
import com.xstory.storysnap.app.view.story.StoryActivity
import com.xstory.storysnap.app.view.welcome.WelcomeActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var storyPreferences: StoryPreferences
    private lateinit var adapter: HomeAdapter
    private val homeViewModel: HomeViewModel by viewModels {
        ViewModelFactory.getStoryInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storyPreferences = StoryPreferences(this)

        setupLayoutManager()
        setSupportActionBar(binding.toolbar)
        setupViewModel()
        setupStoryAdapter()
    }

    private fun setupViewModel() {
        homeViewModel.loginState.observe(this) { isLoggedIn ->
            if (!isLoggedIn) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        homeViewModel.userToken.observe(this) { token ->
            if (!token.isNullOrEmpty()) {
                fetchStories(token)
            }
        }
    }

    private fun setupStoryAdapter() {
        adapter = HomeAdapter(this, storyPreferences)
        binding.rvStories.adapter = adapter
        binding.progressBar.visibility = View.VISIBLE

        adapter.addLoadStateListener { loadState ->
            binding.progressBar.visibility = if (loadState.source.refresh is LoadState.Loading) {
                View.VISIBLE
            } else {
                View.GONE
            }

            val errorState = loadState.source.refresh as? LoadState.Error
            errorState?.let {
                Snackbar.make(binding.root, "Error loading stories. Retry?", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Retry") { adapter.retry() }
                    .show()
            }
        }
    }

    private fun fetchStories(token: String) {
        homeViewModel.fetchStories(token).observe(this) { result ->
            adapter.submitData(lifecycle, result)
        }
    }

    private fun setupLayoutManager() {
        binding.rvStories.layoutManager = if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager(this, 2)
        } else {
            LinearLayoutManager(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_item, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_story -> {
                navigateToStoryActivity()
                true
            }
            R.id.location -> {
                navigateToMapsActivity()
                true
            }
            R.id.logout -> {
                showLogoutConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToStoryActivity() {
        homeViewModel.userToken.observe(this) { token ->
            if (!token.isNullOrEmpty()) {
                val intent = Intent(this@HomeActivity, StoryActivity::class.java)
                intent.putExtra(StoryActivity.TOKEN, token)
                startActivity(intent)
            }
        }
    }

    private fun navigateToMapsActivity() {
        homeViewModel.userToken.observe(this) { token ->
            if (!token.isNullOrEmpty()) {
                val intent = Intent(this@HomeActivity, MapsActivity::class.java)
                intent.putExtra(MapsActivity.MAPS_TOKEN, token)
                startActivity(intent)
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout_title))
            .setMessage(getString(R.string.logout_message))
            .setPositiveButton(getString(R.string.logout_confirm)) { _, _ ->
                homeViewModel.logout()
            }
            .setNegativeButton(getString(R.string.logout_cancel), null)
            .create()
            .show()
    }
}
