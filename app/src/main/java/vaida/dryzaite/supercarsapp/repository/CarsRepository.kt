package vaida.dryzaite.supercarsapp.repository

import io.reactivex.Observable
import vaida.dryzaite.supercarsapp.network.ApiCar
import vaida.dryzaite.supercarsapp.network.SparkApiService
import javax.inject.Inject

class CarsRepository @Inject constructor(
    private val service: SparkApiService
) : CarsRepositoryInterface {

    override fun getCarsFromApi(): Observable<List<ApiCar>> {
        return service.getAvailableCarsObservable()
    }
}