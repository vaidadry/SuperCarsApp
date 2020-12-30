package vaida.dryzaite.supercarsapp.ui.carlist

import android.Manifest
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.tbruyelle.rxpermissions2.RxPermissions
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.car_list_fragment.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import timber.log.Timber
import vaida.dryzaite.supercarsapp.R
import vaida.dryzaite.supercarsapp.databinding.CarListFragmentBinding
import vaida.dryzaite.supercarsapp.utils.Status
import vaida.dryzaite.supercarsapp.ui.carlist.SortDirection.ASCENDING
import vaida.dryzaite.supercarsapp.ui.carlist.SortDirection.TITLE
import javax.inject.Inject

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@AndroidEntryPoint
class CarListFragment @Inject constructor(
    private val carListAdapter: CarListAdapter
) : Fragment() {

    private val viewModel: CarListViewModel by viewModels()
    private lateinit var binding: CarListFragmentBinding
    private val disposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.car_list_fragment, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        requestPermissions()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)

        viewModel.setupCarList()
        setupRecyclerView()
        addListDividerDecoration()
        subscribeToObservers()

        initFilterByPlateNumber()
        initBatteryLevelFilterListener()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.filter_top_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_by_distance -> {
                viewModel.onSortMenuItemClicked()
            }
            R.id.filter -> {
                viewModel.onFilterMenuItemClicked()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        binding.carListRv.apply {
            adapter = carListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun addListDividerDecoration() {
        binding.carListRv.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )
    }

    private fun subscribeToObservers() {

        observeCarList()
        observeNetworkCallStatus()
        observeFilterMenuClick()
        observeSortMenuClick()
    }

    // show/hide filters, on icon clicked
    private fun observeFilterMenuClick() {
        viewModel.filterMenuItemClickCount.observe(viewLifecycleOwner, {
            when (it) {
                1 -> binding.filterContainer.isVisible = true
                2 -> {
                    binding.filterContainer.isVisible = false
                    viewModel.onFilterMenuItemClickCompleted()
                }
            }
        })
    }

    // handle sort icon clicks
    private fun observeSortMenuClick() {
        viewModel.sortMenuItemClickCount.observe(viewLifecycleOwner, {
            when (it) {
                1 -> viewModel.rearrangeCars(ASCENDING)
                2 -> {
                    viewModel.rearrangeCars(TITLE)
                    viewModel.onSortMenuItemClickCompleted()
                }
            }
        })
    }

    private fun observeCarList() {
        viewModel.cars.observe(viewLifecycleOwner, {
            carListAdapter.cars = it ?: arrayListOf()
            checkForEmptyState()
        })
    }

    // Ui changes based on Network status
    private fun observeNetworkCallStatus() {

        viewModel.apiCallStatus.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled()?.let { result ->
                when (result.status) {

                    Status.SUCCESS -> {
                        binding.progressBar.isVisible = false
                        binding.retryButton.isVisible = false
                        binding.carListRv.alpha = 1f
                    }
                    Status.ERROR -> {
                        Snackbar.make(
                            requireActivity().rootLayout,
                            result.message ?: getString(R.string.unknown_error),
                            Snackbar.LENGTH_LONG
                        ).show()
                        binding.retryButton.isVisible = true
                        binding.progressBar.isVisible = false
                        binding.carListRv.alpha = 0.5f
                    }
                    Status.LOADING -> {
                        binding.retryButton.isVisible = false
                        binding.progressBar.isVisible = true
                        binding.carListRv.alpha = 1f
                    }
                }
            }
        })
    }

    // if no items, empty state text is shown
    private fun checkForEmptyState() {
        binding.emptyState.isInvisible = carListAdapter.itemCount != 0
    }

    // SearchView Listener
    private fun initFilterByPlateNumber() {
        binding.plateSearch.setOnCloseListener(object : SearchView.OnCloseListener {
            override fun onClose(): Boolean {
                binding.plateSearch.setQuery("", true)
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
        binding.result.text = String.format(getString(R.string.battery_remaining_text_view), 0)
        binding.batteryFilter.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                batteryLevel: Int,
                fromUser: Boolean
            ) {
                binding.result.text = String.format(
                    getString(R.string.battery_remaining_text_view), batteryLevel)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null) {
                    viewModel.updateBatteryFilterValue(seekBar.progress)
                }
            }
        })
    }

    private fun requestPermissions() {
        RxPermissions(requireActivity())
            .request(Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe({ isGranted ->
                if (isGranted) {
                    viewModel.startLocationUpdates()
                    viewModel.startSynchronization()
                } else {
                    Snackbar.make(car_list_layout, getString(R.string.warning_need_to_accept_location_permissions), Snackbar.LENGTH_LONG).show()
                    requestPermissions()
                }
            }, {
                Timber.e(it.toString())
            }).addTo(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }
}