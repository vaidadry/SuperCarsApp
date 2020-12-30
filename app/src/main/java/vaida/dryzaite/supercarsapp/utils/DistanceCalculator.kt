package vaida.dryzaite.supercarsapp.utils

import android.location.Location

// areal distance calculator between two coordinates
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val userLoc = Location("locA")
    userLoc.latitude = lat1
    userLoc.longitude = lon1

    val carLoc = Location("locB")
    carLoc.latitude = lat2
    carLoc.longitude = lon2

    return userLoc.distanceTo(carLoc)
}