package com.example.tierapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.tierapp.R
import com.example.tierapp.databinding.ActivityMainBinding
import com.example.tierapp.domain.toCluster
import com.example.tierapp.model.Data
import com.example.tierapp.ui.clusters.Cluster
import com.example.tierapp.utils.LifecycleProperty.Companion.lifecycleProperty
import com.example.tierapp.utils.networkutils.UiResult
import com.example.tierapp.viewmodels.MainActivityViewModel
import com.example.tierapp.viewmodels.MainState
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.clustering.ClusterManager
import com.tbruyelle.rxpermissions3.RxPermissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val REQUEST_CODE = 2035
        const val TIMER_INTERVAL: Long = 5 * 60 * 1000
        const val MINIMUM_DELAY: Long = 3 * 60 * 1000
    }

    private var binding: ActivityMainBinding by lifecycleProperty()
    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    private lateinit var snackbar: Snackbar
    private lateinit var clusterManager: ClusterManager<Cluster>
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var rxPermission: RxPermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityMainBinding.inflate(layoutInflater).also {
            this.binding = it
        }.root)
        val mapsFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapsFragment.getMapAsync(this)

        rxPermission = RxPermissions(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        lifecycleScope.launchWhenResumed {
            mainActivityViewModel.connectivityState.collect(::manageConnectivityState)
        }

        getCurrentUserLocation()

        lifecycleScope.launchWhenResumed {
            mainActivityViewModel.viewState.collect { state ->
                if (state.showLoading) {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.loading_vehicles),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        rxPermission.request(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
            .subscribe { granted ->
                if (granted) {
                    getCurrentUserLocation()
                }
            }
    }

    private fun getCurrentUserLocation() {
        val request = LocationRequest.create().apply {
            this.interval = TIMER_INTERVAL
            this.fastestInterval = MINIMUM_DELAY
            this.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val permission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permission == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    mainActivityViewModel.setUserLastLocation(locationResult.lastLocation)
                }
            }, Looper.getMainLooper())
        }
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_CODE
            )
        }
    }


    private fun setUpClusterer(output: List<Data>) {
        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = ClusterManager(this, map)

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)
        addItems(output)
    }

    private fun addItems(output: List<Data>) {
        clusterManager.addItems(output.toCluster())
        clusterManager.cluster()
    }

    private fun manageConnectivityState(connectivityState: MainState.ConnectivityState) {
        when (connectivityState) {
            MainState.ConnectivityState.Offline -> {
                snackbar = Snackbar.make(
                    binding.root,
                    R.string.main_snackbar_message_offline,
                    Snackbar.LENGTH_INDEFINITE
                )
                snackbar.setAction(
                    R.string.main_snackbar_button_settings
                ) {
                    startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                        this.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    })
                }
                snackbar.show()
            }
            MainState.ConnectivityState.Online -> {
                if (this::snackbar.isInitialized && snackbar.isShown)
                    snackbar.dismiss()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        checkPermissions()
        lifecycleScope.launchWhenResumed {
            mainActivityViewModel.userLocation.collect { location ->
                googleMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(location.latitude, location.longitude))
                        .title("You position")
                )
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location.latitude,
                            location.longitude
                        ), 3f
                    )
                )
            }
        }

        mainActivityViewModel.uiResult.observe(this) { result ->
            when (result) {
                is UiResult.Success -> {
                    setUpClusterer((result.output as List<Data>).also {
                        mainActivityViewModel.setCurrentLocations(
                            it
                        )
                    })
                }
                is UiResult.Error -> {
                    snackbar = Snackbar.make(
                        binding.root,
                        R.string.loading_vehicles_went_wrong,
                        Snackbar.LENGTH_INDEFINITE
                    )
                    snackbar.setAction(
                        R.string.retry
                    ) {
                        mainActivityViewModel.getVehicles()
                    }
                    snackbar.show()
                }
                is UiResult.Loading -> {}
            }
        }

        lifecycleScope.launchWhenResumed {
            mainActivityViewModel.nearestPoint.collectLatest {
                Log.d("TAG", "onMapReady: nearest point $it")
                binding.battery.text = it.attributes.batteryLevel.toString().plus("%")
                binding.vehicleType.text = it.attributes.vehicleType
            }
        }

    }
}