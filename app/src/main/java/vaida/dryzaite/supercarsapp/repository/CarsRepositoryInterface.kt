package vaida.dryzaite.supercarsapp.repository

import androidx.lifecycle.LiveData
import vaida.dryzaite.supercarsapp.model.Car

interface CarsRepositoryInterface {

    suspend fun insertCars(cars: List<Car>)

    fun getCars(): LiveData<List<Car>>

    fun getCarsByDistanceAsc(): LiveData<List<Car>>

    suspend fun clearCars()
}