package vaida.dryzaite.supercarsapp.ui.carlist

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.car_list_fragment.*
import vaida.dryzaite.supercarsapp.R
import vaida.dryzaite.supercarsapp.databinding.CarListFragmentBinding
import vaida.dryzaite.supercarsapp.utils.Status
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CarListFragment @Inject constructor(
    private val carListAdapter: CarListAdapter
) : Fragment() {

    private val viewModel: CarListViewModel by viewModels()
    private lateinit var binding: CarListFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.car_list_fragment, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)

        viewModel.startSynchronization()

        setupRecyclerView()
        subscribeToObservers()

        initFilterByPlateNumber()
        initBatteryLevelFilterListener()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.filter_top_menu, menu)
        binding.toolbar.overflowIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_filter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.filter_by_plate -> {
                hideShowSearchByPlate()
            }

            R.id.filter_by_battery -> {
                hideShowFilterByBattery()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        car_list_rv.apply {
            adapter = carListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun subscribeToObservers() {

        observeDatabase()
        observeNetworkCallStatus()

        updateRvByBatteryLevel()
        updateRvByPlateNumber()
    }

    private fun observeDatabase() {
        viewModel.availableCarsFromDb.observe(viewLifecycleOwner, {
            carListAdapter.cars = it ?: arrayListOf()
            checkForEmptyState()
        })
    }

    //Ui changes based on Network status
    private fun observeNetworkCallStatus() {

        viewModel.apiCallStatus.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {

                    Status.SUCCESS -> {
                        progress_bar.visibility = View.GONE
                        retry_button.visibility = View.GONE
                        car_list_rv.alpha = 1f
                    }
                    Status.ERROR -> {
                        Snackbar.make(
                            requireActivity().rootLayout,
                            result.message ?: "Unknown error occurred",
                            Snackbar.LENGTH_LONG
                        ).show()
                        retry_button.visibility = View.VISIBLE
                        progress_bar.visibility = View.GONE
                        car_list_rv.alpha = 0.5f
                    }
                    Status.LOADING -> {
                        retry_button.visibility = View.GONE
                        progress_bar.visibility = View.VISIBLE
                        car_list_rv.alpha = 1f
                    }
                }
            }
        })
    }

    //if no items, empty state text is shown
    private fun checkForEmptyState() {
        binding.emptyState.visibility =
            if (carListAdapter.itemCount == 0) View.VISIBLE else View.INVISIBLE
    }

    private fun hideShowSearchByPlate() {
        binding.plateSearch.visibility =
            if (plate_search.visibility == View.GONE) View.VISIBLE else View.GONE
        binding.batteryFilter.visibility = View.GONE
        binding.result.visibility = View.GONE
    }

    private fun hideShowFilterByBattery() {
        binding.batteryFilter.visibility =
            if (battery_filter.visibility == View.GONE) View.VISIBLE else View.GONE
        binding.result.visibility =
            if (result.visibility == View.GONE) View.VISIBLE else View.GONE
        binding.plateSearch.visibility = View.GONE
    }

    // SearchView Listener
    private fun initFilterByPlateNumber() {
        binding.plateSearch.setOnCloseListener(object : SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                binding.plateSearch.setQuery("", true);
                return true
            }
        })

        binding.plateSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.updatePlateNumberFilterValue(query ?: "")
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updatePlateNumberFilterValue(newText ?: "")
                return false
            }
        })
    }

    // listening to changes of slider, showing result in TV and sending data to VM
    private fun initBatteryLevelFilterListener() {
        binding.batteryFilter.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                batteryLevel: Int,
                fromUser: Boolean
            ) {
                binding.result.text = String.format("%d / 100", batteryLevel)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    viewModel.updateBatteryFilterValue(seekBar.progress)
                }
            }
        })
    }

    // observing Slider value change, filtering DB data, updating adapter
    private fun updateRvByBatteryLevel() {
        viewModel.batteryLevel.observe(viewLifecycleOwner, {

            viewModel.availableCarsFromDb.observe(viewLifecycleOwner, { listFromDb ->

                val filteredByBatteryList = listFromDb.filter { car ->
                    car.batteryPercentage >= viewModel.batteryLevel.value ?: 0
                }
                carListAdapter.cars = filteredByBatteryList
            })
        })
    }

    // observing SearchView value change, filtering DB data, updating adapter
    private fun updateRvByPlateNumber() {
        viewModel.searchQuery.observe(viewLifecycleOwner, {

            viewModel.availableCarsFromDb.observe(viewLifecycleOwner, { listFromDb ->
                val filteredByPlateList = listFromDb.filter { car ->
                    // adjusting formatting
                    car.plateNumber.toLowerCase(Locale.ROOT)
                        .contains(viewModel.searchQuery.value!!.toLowerCase(Locale.ROOT))
                }
                carListAdapter.cars = filteredByPlateList
            })
        })
    }

}
