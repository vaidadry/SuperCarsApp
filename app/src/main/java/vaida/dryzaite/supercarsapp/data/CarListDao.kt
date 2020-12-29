package vaida.dryzaite.supercarsapp.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import vaida.dryzaite.supercarsapp.model.Car

@Dao
interface CarListDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cars: List<Car>)

    @Query("SELECT * FROM car_list")
    fun getCars(): LiveData<List<Car>>

    @Query("DELETE FROM car_list")
    suspend fun clearCars()

}