package vaida.dryzaite.supercarsapp.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import vaida.dryzaite.supercarsapp.ui.carlist.CarListAdapter
import vaida.dryzaite.supercarsapp.ui.carlist.CarListFragment
import javax.inject.Inject

// fragment factory fo all fragments - to inject necessary dependencies into fragments (just as VMFactories)
// should be adjusted by custom fragment needs

class FragmentFactory  @Inject constructor(
    private val carListAdapter: CarListAdapter
): FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when(className) {
            CarListFragment::class.java.name -> CarListFragment(carListAdapter)
            else -> super.instantiate(classLoader, className)
        }
    }
}