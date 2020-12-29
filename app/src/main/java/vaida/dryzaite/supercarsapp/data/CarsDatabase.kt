package vaida.dryzaite.supercarsapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import vaida.dryzaite.supercarsapp.model.Car

@Database(
    entities = [Car::class],
    version = 1,
    exportSchema = false)
abstract class CarsDatabase : RoomDatabase() {

    abstract fun carListDao(): CarListDao

}