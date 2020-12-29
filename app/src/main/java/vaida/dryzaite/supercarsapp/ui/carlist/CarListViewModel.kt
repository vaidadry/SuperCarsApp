package vaida.dryzaite.supercarsapp.ui.carlist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import vaida.dryzaite.supercarsapp.model.Car
import vaida.dryzaite.supercarsapp.repository.CarsRepositoryInterface
import vaida.dryzaite.supercarsapp.utils.CarsSyncManager
import vaida.dryzaite.supercarsapp.utils.Event
import vaida.dryzaite.supercarsapp.utils.Resource
import vaida.dryzaite.supercarsapp.utils.Status
import kotlin.concurrent.fixedRateTimer

class CarListViewModel @ViewModelInject constructor(
    private val repository: CarsRepositoryInterface,
    private val carsManager: CarsSyncManager
) : ViewModel() {

    // to hold value of API data loading Status (not Event)
    private val _networkDataLoadingStatus = MutableLiveData<Status>()

    // to hold value of network Status & data retrieved
    private val _apiCallStatus = MutableLiveData<Event<Resource<List<Car>?>>>()
    val apiCallStatus: LiveData<Event<Resource<List<Car>?>>> = _apiCallStatus

    private val _batteryLevel = MutableLiveData<Int>(0)
    val batteryLevel: LiveData<Int> = _batteryLevel

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    val availableCarsFromDb = repository.getCars()

    fun startSynchronization() {
        _apiCallStatus.value = Event(Resource.loading(null))
        _networkDataLoadingStatus.value = Status.LOADING

        fixedRateTimer(period = 5000L) {

            viewModelScope.launch {

                val response = carsManager.getNetworkCallStatus()

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
        _searchQuery.value = searchQuery
    }

}