package vaida.dryzaite.supercarsapp.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.patloew.colocation.CoLocation
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import vaida.dryzaite.supercarsapp.BuildConfig
import vaida.dryzaite.supercarsapp.R
import vaida.dryzaite.supercarsapp.network.SparkApiService
import vaida.dryzaite.supercarsapp.repository.CarsRepository
import vaida.dryzaite.supercarsapp.repository.CarsRepositoryInterface
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

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
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(BuildConfig.BASE_URL)
            .build()
            .create(SparkApiService::class.java)
    }

    @Singleton
    @Provides
    fun provideCarsRepository(
        service: SparkApiService
    ) = CarsRepository(service) as CarsRepositoryInterface

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.ic_broken)
    )

    @Singleton
    @Provides
    fun provideCoLocation(
        @ApplicationContext context: Context
    ) = CoLocation.from(context)
}