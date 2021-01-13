package vaida.dryzaite.supercarsapp.utils

import android.location.Location
import vaida.dryzaite.supercarsapp.model.Car
import vaida.dryzaite.supercarsapp.network.ApiCar

import javax.inject.Inject

class CarsSyncManager @Inject constructor() {

    fun convertApiCarToCar(apiCar: ApiCar, userLocation: Location?) = with(apiCar) {
        Car(
            id,
            plateNumber,
            location.latitude,
            location.longitude,
            calculateCarDistance(
                userLocation?.latitude,
                userLocation?.longitude,
                location.latitude,
                location.longitude
            ),
            model.title,
            model.photoUrl,
            batteryPercentage
        )
    }

    private fun calculateCarDistance(lat1: Double?, lon1: Double?, lat2: Double, lon2: Double): Float? {
        return if (lat1 != null && lon1 != null) {
            calculateDistance(lat1, lon1, lat2, lon2)
        } else {
            null
        }
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}
