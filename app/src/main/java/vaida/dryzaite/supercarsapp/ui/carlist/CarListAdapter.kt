package vaida.dryzaite.supercarsapp.ui.carlist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import kotlinx.android.synthetic.main.car_list_item.view.*
import vaida.dryzaite.supercarsapp.R
import vaida.dryzaite.supercarsapp.model.Car
import javax.inject.Inject
import kotlin.math.roundToInt

class CarListAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<CarListAdapter.CarListViewHolder>() {

    class CarListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val diffCallback = object : DiffUtil.ItemCallback<Car>() {
        override fun areItemsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Car, newItem: Car): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.title == newItem.title &&
                    oldItem.plateNumber == newItem.plateNumber &&
                    oldItem.batteryPercentage == newItem.batteryPercentage &&
                    oldItem.photoUrl == newItem.photoUrl &&
                    oldItem.longitude == newItem.longitude &&
                    oldItem.latitude == newItem.latitude
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var cars: List<Car>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarListViewHolder {
        return CarListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.car_list_item,
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CarListViewHolder, position: Int) {
        val car = cars[position]
        holder.itemView.apply {
            glide.load(car.photoUrl).into(car_image_iv)
            model_plate_tv.text = car.plateNumber
            model_title_tv.text = car.title
            battery_percentage.text = "${car.batteryPercentage} %"
            val distanceTextFormatter =
                when (car.distance?.roundToInt()) {
                    in 1..1000 -> context.getString(R.string.distance_m_placeholder).format(car.distance)
                    in 1000..10000 -> context.getString(R.string.distance_km_placeholder).format(car.distance?.div(1000))
                    in 10000..1000000000 -> context.getString(R.string.distance_km_placeholder_rounded).format(car.distance?.div(1000)?.roundToInt())
                    else -> context.getString(R.string.no_data_available)
                }
            distance_from_user.text = distanceTextFormatter
        }
    }

    override fun getItemCount(): Int {
        return cars.size
    }
}