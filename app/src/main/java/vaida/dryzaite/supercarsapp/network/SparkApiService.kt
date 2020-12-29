package vaida.dryzaite.supercarsapp.network

import retrofit2.Response
import retrofit2.http.GET

interface SparkApiService {

    @GET("/api/mobile/public/availablecars")
    suspend fun getAvailableCars() : Response<List<ApiCar>>
}