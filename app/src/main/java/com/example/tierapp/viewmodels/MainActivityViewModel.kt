package com.example.tierapp.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import androidx.lifecycle.*
import com.example.tierapp.MainRepository
import com.example.tierapp.domain.toData
import com.example.tierapp.domain.toLatLn
import com.example.tierapp.model.Data
import com.example.tierapp.utils.DispatcherProvider
import com.example.tierapp.utils.networkutils.UiResult
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.utils.sphericalDistance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    application: Application,
    private val dispatcherProvider: DispatcherProvider,
    private val mainRepository: MainRepository,
) : ViewModel() {
    private val connectivityManager: ConnectivityManager = application.getSystemService()
        ?: throw IllegalStateException("Application can not access to `ConnectivityManager`")

    private val _connectivityState: MutableStateFlow<MainState.ConnectivityState>
    val connectivityState: StateFlow<MainState.ConnectivityState>
        get() = _connectivityState

    private val networkCallback = DefaultNetworkCallback()

    private val _viewState = MutableStateFlow(ViewState())
    val viewState get() = _viewState

    private val _uiResult = MutableLiveData<UiResult<*>>()
    val uiResult get() = _uiResult

    private val _userLocation = MutableSharedFlow<Location>()
    val userLocation get() = _userLocation

    private var currentLocations = MutableStateFlow(listOf<Data>())

    fun setCurrentLocations(locations: List<Data>) {
        viewModelScope.launch(dispatcherProvider.io) {
            currentLocations.emit(locations)
        }
    }

    init {
        val hasInternet =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        this._connectivityState = MutableStateFlow(
            if (hasInternet == true) MainState.ConnectivityState.Online
            else MainState.ConnectivityState.Offline
        )
        this.connectivityManager.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            networkCallback
        )
        getVehicles()
    }

    fun setUserLastLocation(location: Location) {
        viewModelScope.launch(dispatcherProvider.io) {
            _userLocation.emit(location)
        }
    }

    fun getVehicles() {
        viewModelScope.launch(dispatcherProvider.io) {
            _viewState.update { it.copy(showLoading = true) }
            val vehicles = mainRepository.getVehicles()
            _uiResult.postValue(vehicles)
            _viewState.update { it.copy(showLoading = false) }
        }
    }

    val nearestPoint = combine(_userLocation, currentLocations) { userLocation, locations ->
        var nearestLocation = LatLng(userLocation.latitude, userLocation.longitude).toData()
        var minDistance = Double.MAX_VALUE
        userLocation.let {
            if (locations.isNotEmpty()) {
                locations.forEach {
                    val difference = it.toLatLn().sphericalDistance(nearestLocation.toLatLn())
                    if (difference <= minDistance) {
                        nearestLocation = it
                        minDistance = difference
                    }
                }
            }
        }
        nearestLocation
    }

    private inner class DefaultNetworkCallback : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val hasInternet = networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            )
            viewModelScope.launch {
                _connectivityState.emit(if (hasInternet) MainState.ConnectivityState.Online else MainState.ConnectivityState.Offline)
            }
        }

        override fun onLost(network: Network) {
            val hasInternet =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                    ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            viewModelScope.launch {
                _connectivityState.emit(
                    if (hasInternet == true) MainState.ConnectivityState.Online
                    else MainState.ConnectivityState.Offline
                )
            }
        }

    }
}

data class ViewState(val showLoading: Boolean = false)

sealed interface MainState {
    enum class ConnectivityState : MainState {
        Offline, Online
    }
}