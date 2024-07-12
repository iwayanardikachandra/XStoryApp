package com.xstory.storysnap.app.view.maps

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityMapsBinding
import com.xstory.storysnap.app.data.handler.GeneralHandler
import com.xstory.storysnap.app.data.remote.response.ListStoryItem
import com.xstory.storysnap.app.data.remote.response.StoryResponse
import com.xstory.storysnap.app.data.factory.ViewModelFactory
import com.xstory.storysnap.app.view.home.HomeActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val token: String by lazy {
        intent.getStringExtra(MAPS_TOKEN) ?: throw IllegalArgumentException("Token must be provided")
    }
    private val mapsViewModel: MapsViewModel by viewModels {
        ViewModelFactory.getMapsInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.toolbar.overflowIcon?.setColorFilter(resources.getColor(R.color.white), PorterDuff.Mode.SRC_ATOP)
        setupToolbar()
        registerPermissionResultHandler()
    }

    private fun registerPermissionResultHandler() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
            title = "Maps"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        startActivity(Intent(this, HomeActivity::class.java))
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mMap.mapType = when (item.itemId) {
            R.id.normal_type -> GoogleMap.MAP_TYPE_NORMAL
            R.id.satellite_type -> GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrain_type -> GoogleMap.MAP_TYPE_TERRAIN
            R.id.hybrid_type -> GoogleMap.MAP_TYPE_HYBRID
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupMapSettings()
        addInitialMarkerAndLocation()
    }

    private fun setupMapSettings() {
        mMap.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isIndoorLevelPickerEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMapToolbarEnabled = true
        }
    }

    private fun addInitialMarkerAndLocation() {
        val indonesia = LatLng(-6.21832736208018, 106.80439558334122)
        mMap.addMarker(MarkerOptions().position(indonesia).title("Stadion Gelora Bung Karno"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(indonesia))
        getMyLocation()
        loadStoryLocations()
    }

    private fun loadStoryLocations() {
        lifecycleScope.launch {
            val page = 1
            val size = 20
            mapsViewModel.getStoryLocation(token, page, size).collect { result ->
                handleResult(result)
            }
        }
    }

    private fun handleResult(result: GeneralHandler<StoryResponse>) {
        when (result) {
            is GeneralHandler.Loading -> showLoadingIndicator(true)
            is GeneralHandler.Success -> {
                showLoadingIndicator(false)
                addMarkersToMap(result.data.listStory)
            }
            is GeneralHandler.Error -> {
                showLoadingIndicator(false)
                Toast.makeText(this, "Failure: ${result.error}", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    private fun showLoadingIndicator(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun addMarkersToMap(stories: List<ListStoryItem>) {
        stories.forEach { story ->
            story.lat.let { lat ->
                story.lon.let { lon ->
                    mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(lat, lon))
                            .title(story.name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            .snippet("$lat, $lon")
                    )
                }
            }
        }
    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    companion object {
        const val MAPS_TOKEN = "extra_token"
    }
}
