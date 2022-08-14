package com.example.tierapp.model

data class Attributes(
    val batteryLevel: Int,
    val hasHelmetBox: Boolean,
    val lat: Double,
    val lng: Double,
    val maxSpeed: Int,
    val vehicleType: String
)