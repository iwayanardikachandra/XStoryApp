package com.xstory.storysnap.app.view.home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.StoryItemBinding
import com.xstory.storysnap.app.data.remote.response.ListStoryItem
import com.xstory.storysnap.app.local.pref.StoryPreferences
import com.xstory.storysnap.app.view.detail.DetailActivity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class HomeAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val storyPreferences: StoryPreferences
) : PagingDataAdapter<ListStoryItem, HomeAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = StoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding, lifecycleOwner, storyPreferences)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class StoryViewHolder(
        private val binding: StoryItemBinding,
        private val lifecycleOwner: LifecycleOwner,
        private val storyPreferences: StoryPreferences
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ListStoryItem) {
            binding.txUsername.text = data.name
            binding.txDescription.text = data.description
            binding.root.setOnClickListener { openDetailActivity(it.context, data) }
            loadImage(data.photoUrl)

            saveStoryPreference(data)
        }

        private fun openDetailActivity(context: Context, data: ListStoryItem) {
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_STORY, data)
            }
            context.startActivity(intent)
        }

        private fun loadImage(photoUrl: String?) {
            Picasso.get()
                .load(photoUrl)
                .resize(800, 800)
                .centerInside()
                .placeholder(R.drawable.image_loading)
                .error(R.drawable.image_error)
                .into(binding.imgAvatar)
        }

        private fun saveStoryPreference(data: ListStoryItem) {
            lifecycleOwner.lifecycleScope.launch {
                storyPreferences.saveStory(data.toString())
            }
        }
    }
}
