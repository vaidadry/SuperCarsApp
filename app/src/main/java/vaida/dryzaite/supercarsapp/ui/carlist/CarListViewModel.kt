package vaida.dryzaite.supercarsapp.ui.carlist

import android.annotation.SuppressLint
import android.location.Location
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.android.gms.location.LocationRequest
import com.patloew.colocation.CoLocation
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import vaida.dryzaite.supercarsapp.model.Car
import vaida.dryzaite.supercarsapp.repository.CarsRepositoryInterface
import vaida.dryzaite.supercarsapp.ui.carlist.SortDirection.ASCENDING
import vaida.dryzaite.supercarsapp.ui.carlist.SortDirection.TITLE
import vaida.dryzaite.supercarsapp.utils.CarsSyncManager
import vaida.dryzaite.supercarsapp.utils.Event
import vaida.dryzaite.supercarsapp.utils.Resource
import vaida.dryzaite.supercarsapp.utils.Status
import java.util.*
import kotlin.concurrent.fixedRateTimer

class CarListViewModel @ViewModelInject constructor(
    repository: CarsRepositoryInterface,
    private val carsManager: CarsSyncManager,
    private val coLocation: CoLocation,
) : ViewModel() {

    // to hold value of API data loading Status (not Event)
    private val _networkDataLoadingStatus = MutableLiveData<Status>()

    // to hold value of network Status & data retrieved
    private val _apiCallStatus = MutableLiveData<Event<Resource<List<Car>?>>>()
    val apiCallStatus: LiveData<Event<Resource<List<Car>?>>> = _apiCallStatus

    //to hold value of UI inputs
    private val _batteryLevel = MutableLiveData<Int>()
    private val _searchQuery = MutableLiveData<String>()

    // Mediator data combines these two and is used in Fragment
    private val availableCarsFromDb = repository.getCars()
    private val availableCarsFromDbDistanceAcs = repository.getCarsByDistanceAsc()
    val cars = MediatorLiveData<List<Car>>()

    private var currentOrder = TITLE

    fun setupCarList() {
        cars.addSource(availableCarsFromDb) { list ->
            if (currentOrder == TITLE) {
                list?.let { carList ->
                    cars.value = carList.filter { car ->
                        car.plateNumber.toLowerCase(Locale.ROOT)
                            .contains(_searchQuery.value ?: "") &&
                                car.batteryPercentage >= _batteryLevel.value ?: 0
                    }
                }
            }
        }

        cars.addSource(availableCarsFromDbDistanceAcs) { list ->
            if (currentOrder == ASCENDING) {
                list?.let { carList ->
                    cars.value = carList.filter { car ->
                        car.plateNumber.toLowerCase(Locale.ROOT)
                            .contains(_searchQuery.value ?: "") &&
                                car.batteryPercentage >= _batteryLevel.value ?: 0
                    }
                }
            }
        }

    }

    fun rearrangeCars(order: SortDirection) = when (order) {
        TITLE -> availableCarsFromDb.value?.let { cars.value = it }
        ASCENDING -> availableCarsFromDbDistanceAcs.value?.let { cars.value = it }
    }.also { currentOrder = order }

    // handling clicks on sort icon -> by title, by distance
    private val _sortMenuItemClickCount = MutableLiveData(0)
    val sortMenuItemClickCount: LiveData<Int> = _sortMenuItemClickCount

    fun onSortMenuItemClicked() {
        _sortMenuItemClickCount.value = _sortMenuItemClickCount.value?.plus(1)
    }

    fun onSortMenuItemClickCompleted() {
        _sortMenuItemClickCount.value = 0
    }

    // handling visibility of filter view on orientation change
    private val _filterMenuItemClickCount = MutableLiveData(0)
    val filterMenuItemClickCount: LiveData<Int> = _filterMenuItemClickCount

    fun onFilterMenuItemClicked() {
        _filterMenuItemClickCount.value = _filterMenuItemClickCount.value?.plus(1)
    }

    fun onFilterMenuItemClickCompleted() {
        _filterMenuItemClickCount.value = 0
    }

    //handling location updates
    private val locationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(5000)
        .setFastestInterval(5000)

    private val _locationUpdates: MutableLiveData<Location> = MutableLiveData()

    private var locationUpdatesJob: Job? = null

    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    @InternalCoroutinesApi
    fun startLocationUpdates() {
        locationUpdatesJob?.cancel()
        locationUpdatesJob = viewModelScope.launch {
            try {
                coLocation.getLocationUpdates(locationRequest).collectLatest { location ->
                    _locationUpdates.postValue(location)
                }
            } catch (e: CancellationException) {
                // Location updates cancelled
            }
            locationUpdatesJob?.cancel()
        }
    }


    // handling sync with database
    fun startSynchronization() {
        _apiCallStatus.value = Event(Resource.loading(null))
        _networkDataLoadingStatus.value = Status.LOADING

        fixedRateTimer(period = 5000L) {

            viewModelScope.launch {

                val response = carsManager.getNetworkCallStatus(_locationUpdates.value)

                _apiCallStatus.value = Event(response)
                _networkDataLoadingStatus.value = response.status
            }
            // cancel sync timer if connection is lost
            if (_networkDataLoadingStatus.value == Status.ERROR) {
                this.cancel()
            }
        }
    }

    // restart sync
    fun onClickRetryButton() {
        startSynchronization()
    }

    fun updateBatteryFilterValue(batteryLevel: Int) {
        _batteryLevel.value = batteryLevel
    }

    fun updatePlateNumberFilterValue(searchQuery: String) {
        _searchQuery.value = searchQuery.toLowerCase(Locale.ROOT)
    }
}

enum class SortDirection {
    ASCENDING,
    TITLE
}