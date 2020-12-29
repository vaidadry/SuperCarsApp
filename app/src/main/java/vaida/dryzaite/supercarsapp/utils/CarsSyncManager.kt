package vaida.dryzaite.supercarsapp.utils

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
    suspend fun getNetworkCallStatus(): Resource<List<Car>?> {
        return try {
            val response = service.getAvailableCars()
            if (response.isSuccessful) {

                response.body()?.let {
                    synchronizeWithDb(it)
                    return@let Resource.success(null)
                } ?: Resource.error("An unknown error occurred", null)
            } else {
                Resource.error("An unknown error occurred", null)
            }
        } catch (e: Exception) {
            return Resource.error("Couldn't reach server. Check your internet connection", null)
        }
    }

    private suspend fun synchronizeWithDb(response: List<ApiCar>) {
        try {
            val cars = response
                .map { convertApiCarToCar(it) }
                .let { carsRepository.insertCars(it) }
        } catch (exception: Exception) {
            Timber.e("EXCEPTION: $exception")
        }
    }

    private fun convertApiCarToCar(apiCar: ApiCar) = with(apiCar) {
        Car(
            id,
            plateNumber,
            location.latitude,
            location.longitude,
            model.title,
            model.photoUrl,
            batteryPercentage
        )
    }
}
