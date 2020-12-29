package vaida.dryzaite.supercarsapp.network

data class ApiCar(
    val id: Int,
    val plateNumber: String,
    val location: ApiCarLocation,
    val model: ApiCarModel,
    val batteryPercentage: Int,
    val batteryEstimatedDistance: Double,
    val isCharging: Boolean,
    val servicePlusEGoPoints: Int
)

data class ApiCarLocation(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val address: String
)

data class ApiCarModel(
    val id: Int,
    val title: String,
    val photoUrl: String,
    val loyaltyPrize: Int,
    val rate: ApiCarRate
)

data class ApiCarRate(
    val isWeekend: Boolean,
    val currency: String,
    val currencySymbol: String,
    val lease: ApiCarLease,
    val reservation: ApiCarReservation
)

data class ApiCarLease(
    val workdays: ApiCarWorkdays,
    val weekends: ApiCarWeekends,
    val kilometerPrice: Double,
    val freeKilometersPerDay: Int
)

data class ApiCarWorkdays(
    val amount: Double,
    val minutes: Int,
    val dailyAmount: Double,
    val minimumPrice: Double,
    val minimumMinutes: Int
)

data class ApiCarWeekends(
    val amount: Double,
    val minutes: Int,
    val dailyAmount: Double,
    val minimumPrice: Double,
    val minimumMinutes: Int
)

data class ApiCarReservation(
    val initialPrice: Double,
    val initialMinutes: Int,
    val extensionPrice: Double,
    val extensionMinutes: Int,
    val longerExtensionPrice: Double,
    val longerExtensionMinutes: Int
)


