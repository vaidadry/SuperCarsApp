package vaida.dryzaite.supercarsapp.utils

import android.location.Location
import timber.log.Timber
import vaida.dryzaite.supercarsapp.model.Car
import vaida.dryzaite.supercarsapp.network.ApiCar
import vaida.dryzaite.supercarsapp.network.SparkApiService
import vaida.dryzaite.supercarsapp.repository.CarsRepositoryInterface
import javax.inject.Inject

// Sync with database data
class CarsSyncManager @Inject constructor(
    private val service: SparkApiService,
    private val carsRepository: CarsRepositoryInterface
) {

    // on successful response, update the database with fresh data
    suspend fun getNetworkCallStatus(locationUpdates: Location?): Resource<List<Car>?> {
        return try {
            val response = service.getAvailableCars()
            if (response.isSuccessful) {

                response.body()?.let {
                    synchronizeWithDb(it, locationUpdates)
                    return@let Resource.success(null)
                } ?: Resource.error(UNKNOWN_ERROR_OCCURRED, null)
            } else {
                Resource.error(UNKNOWN_ERROR_OCCURRED, null)
            }
        } catch (e: Exception) {
            return Resource.error(CANT_REACH_SERVER_ERROR, null)
        }
    }

    private suspend fun synchronizeWithDb(response: List<ApiCar>, location: Location?) {
        try {
            response
                .map { convertApiCarToCar(it, location) }
                .let { carsRepository.insertCars(it) }
        } catch (exception: Exception) {
            Timber.e("EXCEPTION: $exception")
        }
    }

    private fun convertApiCarToCar(apiCar: ApiCar, userLocation: Location?) = with(apiCar) {
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

    private fun calculateCarDistance(
        lat1: Double?, lon1: Double?, lat2: Double, lon2: Double
    ): Float? {
        return if (lat1 != null && lon1 != null) {
            calculateDistance(lat1, lon1, lat2, lon2)
        } else {
            null
        }
    }
}
