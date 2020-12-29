package vaida.dryzaite.supercarsapp.di

import android.content.Context
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import vaida.dryzaite.supercarsapp.BuildConfig
import vaida.dryzaite.supercarsapp.R
import vaida.dryzaite.supercarsapp.data.CarListDao
import vaida.dryzaite.supercarsapp.data.CarsDatabase
import vaida.dryzaite.supercarsapp.network.SparkApiService
import vaida.dryzaite.supercarsapp.repository.CarsRepository
import vaida.dryzaite.supercarsapp.repository.CarsRepositoryInterface
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideCarsDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        CarsDatabase::class.java,
        "cars"
    ).build()


    @Singleton
    @Provides
    fun provideCarListDao(
        database: CarsDatabase
    ) = database.carListDao()

    @Singleton
    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }


    @Singleton
    @Provides
    fun provideSparkApi(moshi: Moshi): SparkApiService {
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BuildConfig.BASE_URL)
            .build()
            .create(SparkApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideCarsRepository(
        dao: CarListDao,
    ) = CarsRepository(dao) as CarsRepositoryInterface


    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.ic_broken)
    )

}