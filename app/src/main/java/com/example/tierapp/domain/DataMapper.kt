package com.example.tierapp.domain

import com.example.tierapp.model.Attributes
import com.example.tierapp.model.Data
import com.example.tierapp.ui.clusters.Cluster
import com.google.android.gms.maps.model.LatLng

fun List<Data>.toCluster(): List<Cluster> {
    return this.map { data ->
        Cluster(
            lat = data.attributes.lat,
            lng = data.attributes.lng,
            title = "battery : ${data.attributes.batteryLevel}%",
            snippet = data.attributes.vehicleType
        )
    }
}

fun Data.toLatLn() : LatLng{
    return LatLng(this.attributes.lat , this.attributes.lng)
}

fun LatLng.toData() : Data{
    return Data(
        attributes = Attributes(
            batteryLevel = 0,
            hasHelmetBox = false,
            lat = this.latitude,
            lng = this.longitude ,
            maxSpeed = 0,
            vehicleType = ""
        ) ,
        id = "" ,
        type = ""
    )
}