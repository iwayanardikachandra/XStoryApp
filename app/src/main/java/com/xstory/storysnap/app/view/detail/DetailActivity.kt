package com.xstory.storysnap.app.view.detail

import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityDetailBinding
import com.squareup.picasso.Picasso
import com.xstory.storysnap.app.data.remote.response.ListStoryItem
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels()

    companion object {
        const val EXTRA_STORY = "extra_story"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()
        setupObserver()
        retrieveStoryDetails()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupObserver() {
        viewModel.story.observe(this) { story ->
            story?.let { displayStoryDetails(it) }
        }
    }

    private fun retrieveStoryDetails() {
        val story = intent.getParcelableExtra<ListStoryItem>(EXTRA_STORY)
        story?.let { viewModel.setStory(it) }
    }

    private fun displayStoryDetails(story: ListStoryItem) {
        binding.txUsername.text = story.name
        binding.txCreatedAt.setLocalDateFormat(story.createdAt)
        binding.txDescription.text = story.description
        binding.imgProfile.loadImage(story.photoUrl)
    }

    private fun ImageView.loadImage(url: String?) {
        Picasso.get()
            .load(url)
            .resize(800, 800)
            .centerInside()
            .placeholder(R.drawable.image_loading)
            .error(R.drawable.image_error)
            .into(this)
    }

    private fun TextView.setLocalDateFormat(dateString: String) {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).apply {
            timeZone = TimeZone.getTimeZone("Asia/Jakarta")
        }
        val date = inputFormat.parse(dateString)
        this.text = date?.let { outputFormat.format(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
