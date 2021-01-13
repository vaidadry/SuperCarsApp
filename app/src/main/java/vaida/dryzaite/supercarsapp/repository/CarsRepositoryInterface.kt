package vaida.dryzaite.supercarsapp.repository

import io.reactivex.Observable
import vaida.dryzaite.supercarsapp.network.ApiCar

interface CarsRepositoryInterface {

    fun getCarsFromApi(): Observable<List<ApiCar>>
}