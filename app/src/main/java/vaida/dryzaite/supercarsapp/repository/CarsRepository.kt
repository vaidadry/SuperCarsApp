package vaida.dryzaite.supercarsapp.repository

import androidx.lifecycle.LiveData
import vaida.dryzaite.supercarsapp.data.CarListDao
import vaida.dryzaite.supercarsapp.model.Car
import javax.inject.Inject

class CarsRepository @Inject constructor(
    private val carListDao: CarListDao
) : CarsRepositoryInterface {

    override suspend fun insertCars(cars: List<Car>) {
        carListDao.insertAll(cars)
    }

    override fun getCars(): LiveData<List<Car>> {
        return carListDao.getCars()
    }


    override suspend fun clearCars() {
        carListDao.clearCars()
    }
}