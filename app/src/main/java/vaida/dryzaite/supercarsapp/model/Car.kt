package vaida.dryzaite.supercarsapp.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "car_list")
data class Car(
    @PrimaryKey @NonNull
    var id: Int,
    var plateNumber: String,
    var latitude: Double,
    var longitude: Double,
    var title: String,
    var photoUrl: String,
    var batteryPercentage: Int
)