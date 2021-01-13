package vaida.dryzaite.supercarsapp.ui.carlist

import android.annotation.SuppressLint
import android.location.Location
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.google.android.gms.location.LocationRequest
import com.patloew.colocation.CoLocation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import vaida.dryzaite.supercarsapp.model.Car
import vaida.dryzaite.supercarsapp.repository.CarsRepositoryInterface
import vaida.dryzaite.supercarsapp.ui.carlist.SortDirection.TITLE
import vaida.dryzaite.supercarsapp.ui.carlist.SortDirection.ASCENDING
import vaida.dryzaite.supercarsapp.utils.CarsSyncManager
import vaida.dryzaite.supercarsapp.utils.Event
import vaida.dryzaite.supercarsapp.utils.Status
import java.util.*

class CarListViewModel @ViewModelInject constructor(
    repository: CarsRepositoryInterface,
    private val carsManager: CarsSyncManager,
    private val coLocation: CoLocation
) : ViewModel() {

    // to hold value of API data loading Status
    private val _networkDataLoadingStatus = MutableLiveData<Event<Status>>()
    val networkDataLoadingStatus: LiveData<Event<Status>> = _networkDataLoadingStatus

    private val disposable = CompositeDisposable()

    // to hold value of UI inputs
    private val _batteryLevel = MutableLiveData<Int>()
    private val _searchQuery = MutableLiveData<String>()
    private val _carList = MutableLiveData<List<Car>>()
    val carList: LiveData<List<Car>> = _carList

    private val availableCarsFromApi = repository.getCarsFromApi()
    private var currentOrder = TITLE

    fun setupCarList(location: Location?) {
        _networkDataLoadingStatus.value = Event(Status.LOADING)
        availableCarsFromApi
            .map { it.map { carsManager.convertApiCarToCar(it, location) } }
            .map { it.filter { car -> car.batteryPercentage >= _batteryLevel.value ?: 0 } }
            .map { it.filter { car -> car.plateNumber.contains(_searchQuery.value ?: "") } }
            .map {
                when (currentOrder) {
                    TITLE -> it.sortedBy { car -> car.plateNumber }
                    ASCENDING -> it.sortedBy { car -> car.distance }
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _networkDataLoadingStatus.value = Event(Status.SUCCESS)
                _carList.postValue(it)
            }, {
                _networkDataLoadingStatus.value = Event(Status.ERROR)
                Timber.e("Observable failed: ${it.message} ")
            }).addTo(disposable)
    }

    fun rearrangeCars(order: SortDirection) {
        currentOrder = order
    }

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

    // handling location updates
    private val locationRequest = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(5000)
        .setFastestInterval(5000)

    private val _locationUpdates: MutableLiveData<Location> = MutableLiveData()
    val locationUpdates: LiveData<Location> = _locationUpdates

    private var locationUpdatesJob: Job? = null

    @SuppressLint("MissingPermission")
    @ExperimentalCoroutinesApi
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
    // restart sync
    fun onClickRetryButton() {
        setupCarList(_locationUpdates.value)
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